package com.example.aura;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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


        TextView lanzarAcercaDe = findViewById(R.id.tv_acerca_de);
        lanzarAcercaDe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lanzarAcercaDe(view);
            }
        });
    }

    public void perfilUsuario(View v) {
        Intent intent = new Intent(ConfiguracionActivity.this, PerfilActivity.class);
        startActivity(intent);
        finish();
    }

    public void cerrarSesion(View view) {

        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Acción para cerrar sesión
                    auth.signOut();
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivity(i);
                    finish(); // Finalizar la actividad actual
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Acción de cancelación (cerrar el diálogo)
                    dialog.dismiss();
                })
                .show();
    }

    public void regresar(View v) {
        Intent i = new Intent(this, HomePage.class);
        startActivity(i);
        finish();
    }

    public void lanzarAcercaDe(View view){
        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }
}

