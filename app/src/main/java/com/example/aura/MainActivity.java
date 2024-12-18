package com.example.aura;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Button stats = findViewById(R.id.luz);
        stats.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EstadisticasActivity.class);
            startActivity(intent);
        });

        Button home = findViewById(R.id.home);
        home.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
        });
    }

    public void configuracion(View v) {
        Intent i = new Intent(this, ConfiguracionActivity.class);
        startActivity(i);
        finish();
    }
}

