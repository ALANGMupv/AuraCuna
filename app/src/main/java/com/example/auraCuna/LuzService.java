package com.example.auraCuna;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class LuzService extends Service {
    private static final String BROKER = "tcp://broker.hivemq.com:1883";
    private static final String TOPIC_LUZ = "cuna/luz";
    private static final String CHANNEL_ID = "LuzNotifications";
    private MqttClient client;
    private MqttConnectOptions options;
    private boolean isLuzOn = false;

    @Override
    public void onCreate() {
        super.onCreate();
        setupMQTT();
    }

    private void setupMQTT() {
        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(BROKER, clientId, null);
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);
        } catch (MqttException e) {
            Log.e("LuzService", "Error al conectar MQTT", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("TOGGLE_LUZ".equals(intent.getAction())) {
            toggleLuz();
        }
        return START_STICKY;
    }

    private void toggleLuz() {
        try {
            if (!client.isConnected()) client.connect(options);
            isLuzOn = !isLuzOn;
            String message = isLuzOn ? "1" : "0";
            client.publish(TOPIC_LUZ, new MqttMessage(message.getBytes()));

            if (isLuzOn) showNotification("Luz encendida", "La luz está encendida");
            else cancelNotification();
        } catch (MqttException e) {
            Log.e("LuzService", "Error al enviar comando MQTT", e);
        }
    }

    private void showNotification(String title, String content) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Luz", NotificationManager.IMPORTANCE_DEFAULT));
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_luz)
                .build();
        manager.notify(1, notification);
    }

    private void cancelNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(1);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
