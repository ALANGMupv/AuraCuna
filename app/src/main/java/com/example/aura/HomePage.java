package com.example.aura;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

//        // Encuentra el Spinner en el diseño
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
