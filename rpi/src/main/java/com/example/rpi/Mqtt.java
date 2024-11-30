package com.example.rpi;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqtt implements MqttCallback {

    private static final String BROKER = "tcp://192.168.1.139:1883"; // Dirección del broker MQTT en la Raspberry Pi
    private static final String CLIENT_ID = "RaspberryPiClient";   // ID del cliente MQTT
    private static final String TOPIC = "cuna/datos";              // Tópico al que nos suscribimos
    private static final int QOS = 1;                              // Nivel de calidad del servicio (QoS)

    private MqttClient client;

    public Mqtt() {
        try {
            // Crear el cliente MQTT y establecer la conexión
            client = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
            client.connect(); // Conectar al broker
            client.setCallback(this); // Establecer el callback
            client.subscribe(TOPIC, QOS); // Suscripción al tópico
            System.out.println("Conectado al broker y suscrito al tópico: " + TOPIC);
        } catch (MqttException e) {
            System.out.println("Error al conectar con el broker: " + e.getMessage());
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

        // Si el mensaje es un JSON, puedes procesarlo aquí (con Gson, Jackson, etc.)
        if (payload.startsWith("{")) {
            System.out.println("Datos en formato JSON: " + payload);
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
