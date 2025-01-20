package com.example.auraCuna.casos_uso;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.auraCuna.R;
import com.example.auraCuna.datos.LuzService;
import com.example.auraCuna.datos.MusicaService;
import com.example.auraCuna.datos.ServoService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.Arrays;
import java.util.List;

public class HomePage extends AppCompatActivity {

    // Botones
    private Button buttonServo;
    private Button buttonLuz;
    private Button buttonMusica;

    private Button button6;  // Botón para la temperatura
    private Button button7;  // Botón para la humedad

    // Cámara
    private ExoPlayer player;
    private PlayerView playerView;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Verificar si el usuario está autenticado antes de mostrar la actividad
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Redirigir al login si el usuario no está autenticado
            Intent intent = new Intent(HomePage.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finalizar HomePage para evitar que se muestre
            return; // Salir para no ejecutar el resto del código
        }

        // Continuar con la inicialización de HomePage si el usuario está autenticado
        setContentView(R.layout.home);

        // Solicitar el permiso de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        buttonServo = findViewById(R.id.servo);
        buttonLuz = findViewById(R.id.luz);
        button6 = findViewById(R.id.button6); // Botón de Temperatura
        button7 = findViewById(R.id.button7); // Botón de Humedad
        buttonMusica = findViewById(R.id.button4);

        buttonServo.setOnClickListener(v -> {
            Intent servoIntent = new Intent(this, ServoService.class);
            servoIntent.setAction("TOGGLE_SERVO");
            startService(servoIntent);
        });

        buttonLuz.setOnClickListener(v -> {
            Intent luzIntent = new Intent(this, LuzService.class);
            luzIntent.setAction("TOGGLE_LUZ");
            startService(luzIntent);
        });

        buttonMusica.setOnClickListener(v -> {
            Intent musicaIntent = new Intent(this, MusicaService.class);
            musicaIntent.setAction("TOGGLE_MUSICA");
            startService(musicaIntent);
        });

        // Floating Action Button (FAB)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, HomePage.class);
            startActivity(intent);
        });

        // Image Button para stats
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(a -> {
            Intent stats = new Intent(HomePage.this, EstadisticasActivity.class);
            startActivity(stats);
        });

        // Image Button para configuración
        ImageButton imageButton2 = findViewById(R.id.imageButton2);
        imageButton2.setOnClickListener(a -> {
            Intent configuracion = new Intent(HomePage.this, ConfiguracionActivity.class);
            startActivity(configuracion);
        });

        obtenerDatosTemperaturaYHumedad();

        // Botón notis
        Button botonNotif = findViewById(R.id.botonNotif);
        botonNotif.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, NotisActivity.class);
            startActivity(intent);
        });

        // Cámara
        // Vincula la vista del reproductor
        playerView = findViewById(R.id.videoPlayer);

        // Inicializa ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // URL del stream (ajusta a tu IP y puerto)
        String streamUrl = "rtsp://192.168.197.201:8554/test";
        Uri uri = Uri.parse(streamUrl);

        // Crea un MediaItem y configúralo en el reproductor
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);

        // Prepara e inicia la reproducción
        player.prepare();
        player.play();

        // Configuración del Spinner
        Spinner spinner = findViewById(R.id.customSpinner);
        List<String> datos = Arrays.asList("Cuna bebé Kevin");
        // Configurar adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner_item, // Layout personalizado para los elementos
                datos
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Listener de selección
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = parent.getItemAtPosition(position).toString();
                Toast.makeText(HomePage.this, "Seleccionaste: " + seleccion, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Acción opcional
            }
        });
    }

    // onDestroy cámara
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }


    private void obtenerDatosTemperaturaYHumedad() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Cunas")
                .document("cuna1")
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w("Firestore", "Error al escuchar los datos.", e);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Obtener los valores de temperatura, humedad y estado
                        double temperatura = documentSnapshot.getDouble("temperatura");
                        double humedad = documentSnapshot.getDouble("humedad");
                        String estado = documentSnapshot.getString("estadoCuna");

                        // Actualizar botones
                        button6.setText(temperatura + "°C");
                        button7.setText(humedad + "%");

                        TextView placeholderText = findViewById(R.id.videoPlaceholderText);

                        if ("ocupada".equals(estado)) {
                            playerView.setVisibility(View.VISIBLE);  // Mostrar el reproductor
                            placeholderText.setVisibility(View.INVISIBLE); // Ocultar el texto
                            if (player != null && !player.isPlaying()) {
                                player.play();
                            }
                        } else {
                            playerView.setVisibility(View.INVISIBLE); // Ocultar el reproductor
                            placeholderText.setVisibility(View.VISIBLE); // Mostrar el texto
                            if (player != null && player.isPlaying()) {
                                player.pause();
                            }
                        }

                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) { // Código de solicitud de permiso
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show();
                // Puedes notificar al usuario que las notificaciones no funcionarán correctamente
            }
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
