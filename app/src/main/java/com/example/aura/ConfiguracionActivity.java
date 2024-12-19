package com.example.aura;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class ConfiguracionActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // Floating Action Button (FAB)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracionActivity.this, HomePage.class);
            startActivity(intent);
        });

        // Image Button para stats
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(a -> {
            Intent stats = new Intent(ConfiguracionActivity.this, EstadisticasActivity.class);
            startActivity(stats);
        });

        // Image Button para configuración
        ImageButton imageButton2 = findViewById(R.id.imageButton2);
        imageButton2.setOnClickListener(a -> {
            Intent configuracion = new Intent(ConfiguracionActivity.this, ConfiguracionActivity.class);
            startActivity(configuracion);
        });

    }

    public void perfilUsuario(View v) {
        Intent intent = new Intent(ConfiguracionActivity.this, PerfilActivity.class);
        startActivity(intent);
        finish();
    }

    public void cerrarSesion(View v) {
        auth.signOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void regresar(View v) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }


}

