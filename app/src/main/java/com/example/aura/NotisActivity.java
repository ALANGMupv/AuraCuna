package com.example.aura;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotisActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotisAdapter adapter;
    private List<Notis> notisList;
    private FirebaseFirestore firestore;

    private final double TEMP_MIN = 20.0; // Temperatura mínima aceptable
    private final double TEMP_MAX = 30.0; // Temperatura máxima aceptable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notis);

        // Inicializar la lista y el adaptador
        notisList = new ArrayList<>();
        adapter = new NotisAdapter(notisList);

        // Configurar el RecyclerView
        recyclerView = findViewById(R.id.recyclerViewNotis);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance();

        // Escuchar cambios en tiempo real
        listenToSensorData();
    }

    private void listenToSensorData() {
        firestore.collection("sensores")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Error al escuchar datos", e);
                        return;
                    }

                    // Recorrer todos los cambios de documento
                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED || change.getType() == DocumentChange.Type.MODIFIED) {
                            // Obtener los datos del documento
                            double temperatura = change.getDocument().getDouble("temperatura");
                            boolean enCuna = change.getDocument().getBoolean("enCuna");
                            Date timestamp = change.getDocument().getTimestamp("timestamp").toDate();

                            // Formatear la hora
                            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(timestamp);

                            // Verificar las condiciones de la temperatura
                            if (temperatura < TEMP_MIN) {
                                addNotification("¡Hace mucho frío!", "Temperatura: " + temperatura + "°C. El bebé podría estar incómodo.", time);
                            } else if (temperatura > TEMP_MAX) {
                                addNotification("¡Hace mucho calor!", "Temperatura: " + temperatura + "°C. El bebé podría estar incómodo.", time);
                            }

                            // Verificar si el bebé está en la cuna
                            if (!enCuna) {
                                addNotification("Bebé fuera de la cuna", "El sensor indica que el bebé no está en la cuna.", time);
                            }
                        }
                    }
                });
    }

    private void addNotification(String title, String description, String time) {
        // Crear un objeto Notis con la notificación
        Notis notis = new Notis(title, description, time);

        // Agregar la nueva notificación al inicio de la lista
        notisList.add(0, notis);

        // Notificar al adaptador que se ha insertado un nuevo item
        adapter.notifyItemInserted(0);
    }
}
