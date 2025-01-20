package com.example.auraCuna.datos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.auraCuna.R;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MusicaService extends Service {
    private static final String BROKER = "tcp://mqtt.eclipseprojects.io:1883";
    private static final String TOPIC_MUSICA = "cuna/musica";
    private static final String CHANNEL_ID = "MusicaNotifications";
    private MqttClient client;
    private MqttConnectOptions options;
    private boolean isMusicPlaying = false;

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
            Log.e("MusicaService", "Error al conectar MQTT", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("TOGGLE_MUSICA".equals(intent.getAction())) {
            toggleMusica();
        }
        return START_STICKY;
    }

    private void toggleMusica() {
        try {
            if (!client.isConnected()) client.connect(options);
            isMusicPlaying = !isMusicPlaying;
            String message = isMusicPlaying ? "1" : "0";
            client.publish(TOPIC_MUSICA, new MqttMessage(message.getBytes()));

            if (isMusicPlaying) showNotification("Música activada", "La música está reproduciéndose");
            else cancelNotification();
        } catch (MqttException e) {
            Log.e("MusicaService", "Error al enviar comando MQTT", e);
        }
    }

    private void showNotification(String title, String content) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Música", NotificationManager.IMPORTANCE_DEFAULT));
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_music)
                .build();
        manager.notify(2, notification);
    }

    private void cancelNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(2);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
