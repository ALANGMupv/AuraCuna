package com.example.aura;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class HomePage extends AppCompatActivity {

    private static final String BROKER = "tcp://192.168.113.201:1883"; // Dirección del broker MQTT
    private static final String TOPIC_SERVO = "cuna/servo";          // Tópico para controlar el servo
    private static final String TOPIC_LUZ = "cuna/luz";              // Tópico para controlar los LEDs
    private static final int QOS = 1;                                // Calidad de servicio MQTT

    private MqttClient client;
    private MqttConnectOptions options;

    private Button buttonServo;
    private Button buttonLuz;

    private boolean isServoMoving = false; // Estado del servo: true = moviéndose, false = detenido
    private boolean isLuzOn = false;      // Estado de los LEDs: true = encendidos, false = apagados
    private boolean isReconnecting = false; // Flag para evitar intentos de reconexión simultáneos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        buttonServo = findViewById(R.id.servo);
        buttonLuz = findViewById(R.id.luz);

        buttonServo.setOnClickListener(v -> toggleServo());
        buttonLuz.setOnClickListener(v -> toggleLuz());

        // Ejecutamos la configuración MQTT en un hilo separado para no bloquear el hilo principal
        new Thread(() -> setupMQTT()).start();
    }

    private void setupMQTT() {
        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(BROKER, clientId, null);
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            // Conectar en un hilo separado para evitar el ANR
            client.connect(options);
            Log.i("MQTT", "Conexión al broker MQTT exitosa.");

            // Suscripción a tópicos
            client.subscribe(TOPIC_SERVO);         // Suscribirse al tópico del servo
            client.subscribe(TOPIC_LUZ);          // Suscribirse al tópico de los LEDs

        } catch (MqttException e) {
            Log.e("MQTT", "Error al conectar al broker: " + e.getMessage(), e);
            reconnectMQTT();  // Intentar reconectar en caso de fallo
        }
    }

    private void toggleServo() {
        new Thread(() -> {
            try {
                // Verificar si el cliente está conectado antes de enviar el mensaje
                if (!client.isConnected()) {
                    Log.w("MQTT", "El cliente no está conectado. Intentando reconectar...");
                    reconnectMQTT(); // Intentar reconectar solo si no hay otra reconexión en progreso
                    return; // Salir del método si el cliente no está conectado
                }

                // Cambiar estado del servo
                isServoMoving = !isServoMoving;

                // Enviar mensaje al broker MQTT en un hilo separado
                String message = isServoMoving ? "1" : "0"; // "1" para mover, "0" para detener
                client.publish(TOPIC_SERVO, new MqttMessage(message.getBytes()));

                Log.i("MQTT", "Estado del servo cambiado: " + message);
            } catch (MqttException e) {
                Log.e("MQTT", "Error al enviar comando MQTT: " + e.getMessage(), e);
            }
        }).start();
    }

    private void toggleLuz() {
        new Thread(() -> {
            try {
                // Verificar si el cliente está conectado antes de enviar el mensaje
                if (!client.isConnected()) {
                    Log.w("MQTT", "El cliente no está conectado. Intentando reconectar...");
                    reconnectMQTT(); // Intentar reconectar solo si no hay otra reconexión en progreso
                    return; // Salir del método si el cliente no está conectado
                }

                // Cambiar estado de los LEDs
                isLuzOn = !isLuzOn;

                // Enviar mensaje al broker MQTT en un hilo separado
                String message = isLuzOn ? "1" : "0"; // "1" para encender, "0" para apagar
                client.publish(TOPIC_LUZ, new MqttMessage(message.getBytes()));

                Log.i("MQTT", "Estado de los LEDs cambiado: " + message);
            } catch (MqttException e) {
                Log.e("MQTT", "Error al enviar comando MQTT: " + e.getMessage(), e);
            }
        }).start();
    }

    private void reconnectMQTT() {
        if (!isReconnecting) {
            isReconnecting = true; // Marcar que estamos en proceso de reconexión
            try {
                // Solo intentar conectar si el cliente no está conectado
                if (!client.isConnected()) {
                    Log.i("MQTT", "Intentando reconectar...");
                    client.connect(options);  // Intentar reconectar
                    Log.i("MQTT", "Reconectado al broker MQTT.");
                }
            } catch (MqttException e) {
                Log.e("MQTT", "Error al reconectar: " + e.getMessage(), e);
                // Si la reconexión falla, espera un poco antes de reintentar
                try {
                    Thread.sleep(2000); // Esperar 2 segundos antes de volver a intentar
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                reconnectMQTT(); // Intentar reconectar nuevamente
            } finally {
                isReconnecting = false; // Marcar que ya hemos terminado el proceso de reconexión
            }
        } else {
            Log.w("MQTT", "Ya hay una reconexión en progreso.");
        }
    }
}
