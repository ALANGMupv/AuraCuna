package com.example.aura;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EstadisticasActivity extends AppCompatActivity {

    private LineChart lineChart;
    private TextView estadoCunaTextView;
    private FirebaseFirestore db;
    private Handler handler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        // Inicializar elementos de la interfaz
        lineChart = findViewById(R.id.grafica_ambiente);
        estadoCunaTextView = findViewById(R.id.textView19);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar Handler y Runnable
        handler = new Handler();

        // Cargar datos iniciales
        cargarDatosActuales();
        cargarDatosHistoricos();

        // Establecer un Runnable para actualizar los datos cada 10 segundos (10,000 ms)
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                cargarDatosActuales(); // Actualiza los datos actuales
                cargarDatosHistoricos(); // Actualiza los datos históricos
                handler.postDelayed(this, 10000); // Llamar al mismo Runnable después de 10 segundos
            }
        };

        // Ejecutar el Runnable inmediatamente al iniciar
        handler.post(updateRunnable);
    }

    private void cargarDatosActuales() {
        db.collection("Cunas").document("cuna1").collection("historico")
                .limit(1) // Obtener solo el primer documento encontrado
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Obtiene el primer documento de la subcolección
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);

                        // Obtiene el estado de la cuna, puede ser "ocupada" o "vacia"
                        String estadoCuna = document.getString("estadoCuna");

                        // Actualiza el TextView con el estado de la cuna
                        estadoCunaTextView.setText(estadoCuna != null ? estadoCuna : "Estado desconocido");
                    } else {
                        estadoCunaTextView.setText("No hay documentos disponibles");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al cargar estado de la cuna", e);
                    estadoCunaTextView.setText("Error al cargar");
                });
    }

    private void cargarDatosHistoricos() {
        db.collection("Cunas").document("cuna1").collection("historico")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Entry> temperaturaEntries = new ArrayList<>();
                    ArrayList<Entry> humedadEntries = new ArrayList<>();
                    double sumaTemperatura = 0;
                    double sumaHumedad = 0;
                    int count = 0;

                    int index = 0;

                    // Primero obtener todos los documentos
                    List<QueryDocumentSnapshot> documentos = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        documentos.add(document);
                    }

                    // Limitar a los últimos 13 documentos (si hay menos de 13, tomar todos)
                    int size = documentos.size();
                    int limit = Math.min(size, 13);

                    // Agregar solo los últimos 13 documentos a las listas de datos
                    for (int i = size - limit; i < size; i++) {
                        QueryDocumentSnapshot document = documentos.get(i);
                        Double temperatura = document.getDouble("temperatura");
                        Double humedad = document.getDouble("humedad");

                        if (temperatura != null && humedad != null) {
                            temperaturaEntries.add(new Entry(index, temperatura.floatValue()));
                            humedadEntries.add(new Entry(index, humedad.floatValue()));

                            // Acumulamos las sumas para el promedio
                            sumaTemperatura += temperatura;
                            sumaHumedad += humedad;
                            count++;
                            index++;
                        }
                    }

                    // Calcular el promedio
                    double promedioTemperatura = sumaTemperatura / count;
                    double promedioHumedad = sumaHumedad / count;

                    // Actualizar los TextView con los promedios
                    TextView textView24 = findViewById(R.id.textView24);  // Promedio de temperatura
                    TextView textView26 = findViewById(R.id.textView26);  // Promedio de humedad

                    textView24.setText(String.format(" %.2f°C", promedioTemperatura));
                    textView26.setText(String.format(" %.2f%%", promedioHumedad));

                    // Configurar DataSets con colores actualizados
                    LineDataSet temperaturaDataSet = new LineDataSet(temperaturaEntries, "Temperatura");
                    temperaturaDataSet.setColor(ContextCompat.getColor(this, R.color.red)); // Rojo
                    temperaturaDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.black));
                    temperaturaDataSet.setLineWidth(2f);

                    // Agregar fondo debajo de la línea de temperatura
                    temperaturaDataSet.setDrawFilled(true);
                    temperaturaDataSet.setFillColor(ContextCompat.getColor(this, R.color.red_light)); // Fondo rojo suave
                    temperaturaDataSet.setFillAlpha(70); // Transparencia del fondo

                    LineDataSet humedadDataSet = new LineDataSet(humedadEntries, "Humedad");
                    humedadDataSet.setColor(ContextCompat.getColor(this, R.color.blue)); // Azul
                    humedadDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.black));
                    humedadDataSet.setLineWidth(2f);

                    // Agregar fondo debajo de la línea de humedad
                    humedadDataSet.setDrawFilled(true);
                    humedadDataSet.setFillColor(ContextCompat.getColor(this, R.color.blue_light)); // Fondo azul suave
                    humedadDataSet.setFillAlpha(70); // Transparencia del fondo

                    // Configurar LineChart
                    LineData lineData = new LineData(temperaturaDataSet, humedadDataSet);
                    lineChart.setData(lineData);

                    // Configuración del gráfico
                    lineChart.setDrawBorders(true);
                    lineChart.setBorderWidth(1f);
                    lineChart.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                    lineChart.getDescription().setText("Datos del Ambiente");
                    lineChart.getDescription().setTextSize(12f);

                    // Configurar el eje Y para que vaya hasta el 0 (incluyendo valores negativos si es necesario)
                    YAxis yAxis = lineChart.getAxisLeft();
                    yAxis.setAxisMinimum(0f); // Asegura que el valor mínimo sea 0
                    yAxis.setGranularity(1f); // Opcional: asegura que el eje Y sea granular y no tenga saltos grandes

                    // Desactivar las leyendas de los ejes derecho y superior
                    lineChart.getAxisRight().setEnabled(false);  // Quitar eje derecho
                    lineChart.getXAxis().setEnabled(false); // Quitar eje superior

                    lineChart.invalidate(); // Refrescar gráfica
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error al cargar datos históricos", e));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el Runnable cuando la actividad se destruya para evitar fugas de memoria
        handler.removeCallbacks(updateRunnable);
    }
}
