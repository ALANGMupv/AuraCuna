package com.example.aura;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditarDatosPersonalesActivity extends AppCompatActivity {

    private Button cerrarBoton, confirmarBoton;
    private EditText nombreNuevo, apellidosNuevos;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_datos);


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) (metrics.widthPixels * 0.9);
        int height = (int) (metrics.heightPixels * 0.65);
        getWindow().setLayout(width, height);

        cerrarBoton = findViewById(R.id.btn_cancelar);
        confirmarBoton = findViewById(R.id.btn_confirmar);
        nombreNuevo = findViewById(R.id.et_nuevo_nombre);
        apellidosNuevos = findViewById(R.id.et_nuevos_apellidos);

        cerrarBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirmarBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizarDatos(view);
            }
        });

    }

    public void actualizarDatos(View view) {
        String nombre = nombreNuevo.getText().toString().trim();
        String apellidos = apellidosNuevos.getText().toString().trim();

        // Verificar qué campo ha cambiado
        String mensaje = "";
        if (!nombre.isEmpty()) {
            mensaje += "¿Quieres cambiar tu nombre a: " + nombre + "? ";
        }
        if (!apellidos.isEmpty()) {
            if (!mensaje.isEmpty()) {
                mensaje += "y ";
            }
            mensaje += "¿Quieres cambiar tus apellidos a: " + apellidos + "?";
        }

        if (!mensaje.isEmpty()) {
            // Mostrar diálogo de confirmación
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(mensaje)
                    .setPositiveButton("Confirmar", (dialog, id) -> {

                        actualizarEnFirestore(nombre, apellidos);
                    })
                    .setNegativeButton("Cancelar", (dialog, id) -> {
                        // Si el usuario cancela, se cierra el diálogo
                        dialog.dismiss();
                    });

            builder.create().show();
        } else {
            // Si no se cambió ningún dato, mostrar mensaje
            Toast.makeText(this, "No se ha modificado ningún dato.", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarEnFirestore(String nombre, String apellidos) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            // Si el usuario no está autenticado, mostrar mensaje de error
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar los datos a actualizar
        Map<String, Object> updates = new HashMap<>();

        if (!nombre.isEmpty()) {
            updates.put("nombre", nombre);
        }
        if (!apellidos.isEmpty()) {
            updates.put("apellido", apellidos);
        }

        // Actualizar en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(usuario.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_LONG).show();

                })
                .addOnFailureListener(e -> {
                    // Mostrar mensaje de error en caso de fallo
                    Toast.makeText(this, "Error al actualizar los datos", Toast.LENGTH_LONG).show();
                });
    }

}
