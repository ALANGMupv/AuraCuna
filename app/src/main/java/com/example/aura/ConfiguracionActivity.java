package com.example.aura;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ConfiguracionActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
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

