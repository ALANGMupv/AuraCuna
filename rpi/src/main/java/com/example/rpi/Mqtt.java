package com.example.rpi;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class Mqtt implements MqttCallback {
    private static final String BROKER = "tcp://192.168.87.90:1883"; // Dirección del broker MQTT en la Raspberry Pi
    private static final String CLIENT_ID = "RaspberryPiClient";   // ID del cliente MQTT
    private static final String TOPIC = "cuna/datos";              // Tópico al que nos suscribimos
    private static final int QOS = 1;                              // Nivel de calidad del servicio (QoS)

    private Firestore db;
    private MqttClient client;


    public Mqtt() {
        try {
            // Inicializar Firebase
            FileInputStream serviceAccount = new FileInputStream("rpi/src/main/res/aura_clave.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://aura-1e33f.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();

            // Crear el cliente MQTT y establecer la conexión
            client = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
            client.connect(); // Conectar al broker
            client.setCallback(this); // Establecer el callback
            client.subscribe(TOPIC, QOS); // Suscripción al tópico
            System.out.println("Conectado al broker y suscrito al tópico: " + TOPIC);
        } catch (MqttException | IOException e) {
            System.out.println("Error al conectar con el broker o inicializar Firebase: " + e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Conexión perdida con el broker MQTT: " + cause.getMessage());
    }


    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // Procesar el mensaje recibido
        String payload = new String(message.getPayload());
        System.out.println("Mensaje recibido en el tópico '" + topic + "': " + payload);

        // Si el mensaje es un JSON, procesarlo
        if (payload.startsWith("{")) {
            System.out.println("Datos en formato JSON: " + payload);

            // Convertir el JSON a un Map
            Map<String, Object> data = new Gson().fromJson(payload, Map.class);

            // Subir los datos a Firestore (servidor)
            ApiFuture<DocumentReference> future = db.collection("SensoresEnviando").add(data);

            // Manejo de la respuesta (bloqueante)
            try {
                DocumentReference documentReference = future.get();
                System.out.println("Datos subidos a Firestore con ID: " + documentReference.getId());
            } catch (Exception e) {
                System.err.println("Error al subir los datos a Firestore: " + e.getMessage());
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Este callback no se usa para suscripciones, pero debe ser implementado
    }

    public static void main(String[] args) {
        Mqtt mqtt = new Mqtt();
        while (true) {
            try {
                // Mantener el programa en ejecución
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
