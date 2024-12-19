package com.example.aura;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class HomePage extends AppCompatActivity {

    private static final String BROKER = "tcp://192.168.113.201:1883";
    private static final String TOPIC_SERVO = "cuna/servo";
    private static final String TOPIC_LUZ = "cuna/luz";
    private static final int QOS = 1;

    private MqttClient client;
    private MqttConnectOptions options;

    private Button buttonServo;
    private Button buttonLuz;

    private boolean isServoMoving = false;
    private boolean isLuzOn = false;
    private boolean isReconnecting = false;

    private static final String CHANNEL_ID = "MQTT_Notifications";

    private Button button6;  // Botón para la temperatura
    private Button button7;  // Botón para la humedad

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        buttonServo = findViewById(R.id.servo);
        buttonLuz = findViewById(R.id.luz);
        button6 = findViewById(R.id.button6); // Botón de Temperatura
        button7 = findViewById(R.id.button7); // Botón de Humedad

        buttonServo.setOnClickListener(v -> toggleServo());
        buttonLuz.setOnClickListener(v -> toggleLuz());

        // Configuración MQTT en un hilo separado
        new Thread(() -> setupMQTT()).start();

        createNotificationChannel();

        // Floating Action Button (FAB)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, HomePage.class);
            startActivity(intent);
        });

        // Image Button para stats
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(a -> {
            Intent stats = new Intent(HomePage.this, HomePage.class);
            startActivity(stats);
        });

        // Image Button para configuración
        ImageButton imageButton2 = findViewById(R.id.imageButton2);
        imageButton2.setOnClickListener(a -> {
            Intent configuracion = new Intent(HomePage.this, ConfiguracionActivity.class);
            startActivity(configuracion);
        });

        obtenerDatosTemperaturaYHumedad();
    }

    private void obtenerDatosTemperaturaYHumedad() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Cunas")
                .document("cuna1")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Firestore", "Error al escuchar los datos.", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            double temperatura = documentSnapshot.getDouble("temperatura");
                            double humedad = documentSnapshot.getDouble("humedad");

                            // Actualizar los botones con los valores de temperatura y humedad
                            button6.setText(temperatura + "°C");
                            button7.setText(humedad + "%");
                        }
                    }
                });
    }

    private void setupMQTT() {
        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(BROKER, clientId, null);
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            client.connect(options);
            Log.i("MQTT", "Conexión al broker MQTT exitosa.");

            client.subscribe(TOPIC_SERVO);
            client.subscribe(TOPIC_LUZ);

        } catch (MqttException e) {
            Log.e("MQTT", "Error al conectar al broker: " + e.getMessage(), e);
            reconnectMQTT();
        }
    }

    private void toggleServo() {
        new Thread(() -> {
            try {
                if (!client.isConnected()) {
                    Log.w("MQTT", "El cliente no está conectado. Intentando reconectar...");
                    reconnectMQTT();
                    return;
                }

                isServoMoving = !isServoMoving;

                String message = isServoMoving ? "1" : "0";
                client.publish(TOPIC_SERVO, new MqttMessage(message.getBytes()));

                Log.i("MQTT", "Estado del servo cambiado: " + message);

                // Guardar el estado del servo
                saveServoState(isServoMoving);

                // Mostrar o cancelar la notificación según el estado del servo
                if (isServoMoving) {
                    showServoNotification();
                } else {
                    cancelServoNotification();
                }

            } catch (MqttException e) {
                Log.e("MQTT", "Error al enviar comando MQTT: " + e.getMessage(), e);
            }
        }).start();
    }

    private void toggleLuz() {
        new Thread(() -> {
            try {
                if (!client.isConnected()) {
                    Log.w("MQTT", "El cliente no está conectado. Intentando reconectar...");
                    reconnectMQTT();
                    return;
                }

                isLuzOn = !isLuzOn;

                String message = isLuzOn ? "1" : "0";
                client.publish(TOPIC_LUZ, new MqttMessage(message.getBytes()));

                Log.i("MQTT", "Estado de los LEDs cambiado: " + message);

                // Guardar el estado de la luz
                saveLuzState(isLuzOn);

                // Mostrar o cancelar la notificación según el estado de los LEDs
                if (isLuzOn) {
                    showLuzNotification();
                } else {
                    cancelLuzNotification();
                }

            } catch (MqttException e) {
                Log.e("MQTT", "Error al enviar comando MQTT: " + e.getMessage(), e);
            }
        }).start();
    }

    // Guardar el estado del servo
    private void saveServoState(boolean isMoving) {
        SharedPreferences sharedPreferences = getSharedPreferences("DeviceStates", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("servoState", isMoving);
        editor.apply();
    }

    // Guardar el estado de la luz
    private void saveLuzState(boolean isOn) {
        SharedPreferences sharedPreferences = getSharedPreferences("DeviceStates", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("luzState", isOn);
        editor.apply();
    }

    private void showServoNotification() {
        Intent intent = new Intent(this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Mecimiento de la cuna")
                .setContentText("La cuna se está moviendo.")
                .setSmallIcon(R.mipmap.ic_cuna)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void cancelServoNotification() {
        if (!isServoMoving) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        }
    }

    private void showLuzNotification() {
        Intent intent = new Intent(this, HomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Luz del bebé")
                .setContentText("Las luces están encendidas.")
                .setSmallIcon(R.mipmap.ic_luz)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, notification);
    }

    private void cancelLuzNotification() {
        if (!isLuzOn) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(2);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MQTT Notifications";
            String description = "Notificaciones relacionadas con el estado de los dispositivos MQTT";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void reconnectMQTT() {
        if (!isReconnecting) {
            isReconnecting = true;
            try {
                if (!client.isConnected()) {
                    Log.i("MQTT", "Intentando reconectar...");
                    client.connect(options);
                    Log.i("MQTT", "Reconectado al broker MQTT.");
                }
            } catch (MqttException e) {
                Log.e("MQTT", "Error al reconectar: " + e.getMessage(), e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                reconnectMQTT();
            } finally {
                isReconnecting = false;
            }
        } else {
            Log.w("MQTT", "Ya hay una reconexión en progreso.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences("DeviceStates", MODE_PRIVATE);
        isServoMoving = sharedPreferences.getBoolean("servoState", false);
        isLuzOn = sharedPreferences.getBoolean("luzState", false);

        if (isServoMoving) {
            showServoNotification();
        } else {
            cancelServoNotification();
        }

        if (isLuzOn) {
            showLuzNotification();
        } else {
            cancelLuzNotification();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
