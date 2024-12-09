package com.example.aura;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class EstadisticasActivity extends AppCompatActivity {

    private LineChart lineChart;

    // Declaramos los TextViews
    private TextView temperaturaTextView;
    private TextView presionTextView;

    // Instancia de FirebaseFirestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        // Inicializamos la instancia de Firestore
        db = FirebaseFirestore.getInstance();

        // Vinculamos los TextViews y el LineChart con sus IDs en el layout
        temperaturaTextView = findViewById(R.id.temperatura);
        presionTextView = findViewById(R.id.Presionado);
        lineChart = findViewById(R.id.lineChart);

        // Llamamos a los métodos para cargar datos
        cargarDatosSensores();
        loadChartData();
    }

    private void cargarDatosSensores() {
        // Referencia a la colección "sensores"
        CollectionReference sensoresRef = db.collection("sensores");

        // Obtenemos todos los documentos de la colección
        sensoresRef.addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
            if (error != null) {
                Log.e("Firestore", "Error al obtener los documentos", error);
                return;
            }

            // Iteramos sobre los documentos de la colección
            for (QueryDocumentSnapshot document : value) {
                String sensorId = document.getId(); // Ejemplo: Sensortemp, Sensorpresion, etc.
                Map<String, Object> data = document.getData();

                // Actualizamos los TextViews según el sensor
                if ("Sensortemp".equals(sensorId)) {
                    String temperatura = data.get("temperatura").toString(); // Asegúrate de que el campo sea correcto
                    temperaturaTextView.setText(temperatura + "°C");
                } else if ("Sensorpresion".equals(sensorId)) {
                    String presion = data.get("presion").toString(); // Asegúrate de que el campo sea correcto
                    presionTextView.setText(presion + " kPa");
                }
            }
        });
    }

    private void loadChartData() {
        db.collection("sensores").document("Sensortest").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtén los datos del documento
                        ArrayList<Entry> entries = new ArrayList<>();
                        ArrayList<Map<String, Object>> datos = (ArrayList<Map<String, Object>>) documentSnapshot.get("datos");

                        if (datos != null) {
                            for (int i = 0; i < datos.size(); i++) {
                                Map<String, Object> dataPoint = datos.get(i);

                                // Obtén el timestamp como Timestamp
                                com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) dataPoint.get("timestamp");

                                // Convertir el Timestamp a un valor numérico (por ejemplo, a milisegundos)
                                long timestampMillis = timestamp != null ? timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000 : 0;

                                // Obtén el valor
                                float value = ((Number) dataPoint.get("value")).floatValue();

                                // Agregar el valor al gráfico
                                entries.add(new Entry(i, value)); // X será la posición; Y, el valor
                            }

                            // Crea el DataSet para la gráfica
                            LineDataSet lineDataSet = new LineDataSet(entries, "Temperatura");
                            lineDataSet.setColor(ContextCompat.getColor(EstadisticasActivity.this, R.color.teal_700));
                            lineDataSet.setValueTextColor(ContextCompat.getColor(EstadisticasActivity.this, R.color.black));

                            // Asocia el DataSet con el LineChart
                            LineData lineData = new LineData(lineDataSet);
                            lineChart.setData(lineData);
                            lineChart.invalidate(); // Refresca la gráfica
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error al cargar datos", e));
    }

}
