package com.example.aura;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
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

    private NotificationManager notificationManager;

    private static final String CANAL_ID = "mi_canal";
    private static final int NOTIFICACION_ID = 1;

    private final double TEMP_MIN = 18.0; // Temperatura mínima aceptable
    private final double TEMP_MAX = 24.0; // Temperatura máxima aceptable
    private final double HUMIDITY_MIN = 35.0; // Humedad mínima aceptable
    private final double HUMIDITY_MAX = 60.0; // Humedad máxima aceptable

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

        // Inicializar NotificationManager
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Crear el canal de notificación para Android 8.0 (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Notificaciones Sensores", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Notificaciones de temperatura, humedad y cuna.");
            notificationManager.createNotificationChannel(notificationChannel);
        }

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
                            double humedad = change.getDocument().getDouble("humedad");
                            boolean enCuna = change.getDocument().getBoolean("enCuna");
                            Date timestamp = change.getDocument().getTimestamp("timestamp").toDate();

                            // Formatear la hora
                            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(timestamp);

                            // Verificar las condiciones de la temperatura
                            if (temperatura < TEMP_MIN) {
                                addNotification("¡Hace mucho frío!", "Temperatura: " + temperatura + "°C. El bebé podría estar incómodo.", time);
                                sendSystemNotification("¡Hace mucho frío!", "Temperatura: " + temperatura + "°C. El bebé podría estar incómodo.");
                            } else if (temperatura > TEMP_MAX) {
                                addNotification("¡Hace mucho calor!", "Temperatura: " + temperatura + "°C. El bebé podría estar incómodo.", time);
                                sendSystemNotification("¡Hace mucho calor!", "Temperatura: " + temperatura + "°C. El bebé podría estar incómodo.");
                            }

                            // Verificar las condiciones de la humedad
                            if (humedad < HUMIDITY_MIN) {
                                addNotification("¡Humedad muy baja!", "Humedad: " + humedad + "%. El ambiente podría estar demasiado seco para el bebé.", time);
                                sendSystemNotification("¡Humedad muy baja!", "Humedad: " + humedad + "%. El ambiente podría estar demasiado seco para el bebé.");
                            } else if (humedad > HUMIDITY_MAX) {
                                addNotification("¡Humedad muy alta!", "Humedad: " + humedad + "%. El ambiente podría ser incómodo para el bebé.", time);
                                sendSystemNotification("¡Humedad muy alta!", "Humedad: " + humedad + "%. El ambiente podría ser incómodo para el bebé.");
                            }

                            // Verificar si el bebé está fuera de la cuna
                            if (!enCuna) {
                                addNotification("Bebé fuera de la cuna", "El sensor indica que el bebé no está en la cuna.", time);
                                sendSystemNotification("Bebé fuera de la cuna", "El sensor indica que el bebé no está en la cuna.");
                            } else {
                                // Si el bebé está en la cuna, mostrar notificación
                                addNotification("Bebé en la cuna", "El bebé está en la cuna.", time);
                                sendSystemNotification("Bebé en la cuna", "El bebé está en la cuna.");
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

    private void sendSystemNotification(String title, String description) {
        // Crear la notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CANAL_ID)
                .setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(android.R.drawable.ic_popup_reminder);

        // Enviar la notificación
        notificationManager.notify(NOTIFICACION_ID, notificationBuilder.build());
    }
}
