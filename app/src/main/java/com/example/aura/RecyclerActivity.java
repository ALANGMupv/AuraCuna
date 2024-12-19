package com.example.aura;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RecyclerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TiendaAdaptador adaptador;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        cargarTiendas();
    }

    private void cargarTiendas() {
        db.collection("tiendas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Tienda> listaTiendas = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Tienda tienda = document.toObject(Tienda.class);
                        listaTiendas.add(tienda);
                    }
                    adaptador = new TiendaAdaptador(listaTiendas);
                    recyclerView.setAdapter(adaptador);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                });
    }
}
