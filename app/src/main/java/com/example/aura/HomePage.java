package com.example.aura;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends AppCompatActivity {

// hola alex
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // Asegúrate de que sea el layout correcto

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        TextView correo = findViewById(R.id.Campo_email);
        correo.setText(usuario.getEmail());

        // Mostrar nombre de usuario
        TextView nombreTextView = findViewById(R.id.Campo_Nombre);
        // Obtener el nombre de usuario de la base de datos
        String userId = usuario.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("name"); //hola

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String nombre = dataSnapshot.getValue(String.class);
                nombreTextView.setText(nombre != null ? nombre : "Nombre no disponible");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePage.this, "Error al cargar el nombre de usuario", Toast.LENGTH_SHORT).show();
            }
        });

        // Configuración de Volley y ImageLoader
        RequestQueue colaPeticiones = Volley.newRequestQueue(this);
        ImageLoader lectorImagenes = new ImageLoader(colaPeticiones, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(10);

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }
        });

        // Foto de usuario
        Uri urlImagen = usuario.getPhotoUrl();
        if (urlImagen != null) {
            NetworkImageView foto = findViewById(R.id.imageView); // Asegúrate de que este ID existe en tu layout
            foto.setImageUrl(urlImagen.toString(), lectorImagenes);
        }

        // Configurar los botones de editar nombre y editar correo
        findViewById(R.id.Editar_Nombre).setOnClickListener(view -> mostrarPopupEditarNombre());
        findViewById(R.id.Editar_Email).setOnClickListener(view -> mostrarPopupEditarEmail());
    }

    public void cerrarSesion(View view) {
        AuthUI.getInstance().signOut(getApplicationContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });
    }

    // Método para lanzar la actividad AcercaDeActivity
    public void lanzarAcercaDe(View view) {
        Intent intent = new Intent(this, AcercaDeActivity.class);
        startActivity(intent);
    }

    // Método para mostrar el popup y editar el nombre
    private void mostrarPopupEditarNombre() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Nombre");

        // Crear un EditText para que el usuario ingrese el nuevo nombre
        final EditText input = new EditText(this);
        input.setHint("Ingrese el nuevo nombre");
        builder.setView(input);

        // Obtener el usuario actual
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        String userId = usuario.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("name"); //aqui el commit

        // Configurar botones del dialog
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = input.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                // Actualizar el nombre en Firebase
                userRef.setValue(nuevoNombre).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Actualizar la vista con el nuevo nombre
                        TextView nombreTextView = findViewById(R.id.Campo_Nombre);
                        nombreTextView.setText(nuevoNombre);
                        Toast.makeText(HomePage.this, "Nombre actualizado a: " + nuevoNombre, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomePage.this, "Error al actualizar el nombre", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(HomePage.this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Método para mostrar el popup y editar el email
    private void mostrarPopupEditarEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Email");

        // Crear un EditText para que el usuario ingrese el nuevo correo
        final EditText input = new EditText(this);
        input.setHint("Ingrese el nuevo correo");
        builder.setView(input);

        // Configurar botones del dialog
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoCorreo = input.getText().toString().trim();
            if (!nuevoCorreo.isEmpty()) {
                // Simular actualización en Firebase
                Toast.makeText(HomePage.this, "Correo simulado actualizado a: " + nuevoCorreo, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomePage.this, "El correo no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
