package com.example.aura;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomePage extends AppCompatActivity {

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

//       // Encuentra el Spinner en el diseño
//        Spinner spinner = findViewById(R.id.customSpinner); // Asegúrate de que el ID coincida con el XML
//
//        // Configura el adaptador para el Spinner
//        @SuppressLint("ResourceType") ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//                this,
//                R.array.spinner_items, // Tu array de elementos en strings.xml
//                R.drawable.custom_spinner_item // Diseño del texto del Spinner
//        );
//
//        // Aplica el estilo al menú desplegable
//        adapter.setDropDownViewResource(R.drawable.custom_spinner_popup);
//
//        // Asigna el adaptador al Spinner
//        spinner.setAdapter(adapter);


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
