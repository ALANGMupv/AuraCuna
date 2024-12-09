package com.example.aura;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ReestablecerContraseñaActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private EditText etCorreo;
    private TextInputLayout tilCorreo;
    private ProgressDialog dialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reestablecer_contrasena);

        // Inicializar FirebaseAuth y Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Referencias de los elementos de la vista
        etCorreo = findViewById(R.id.correo);
        tilCorreo = findViewById(R.id.til_correo);

        // Configurar ProgressDialog
        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Enviando enlace");
        dialogo.setMessage("Por favor espere...");
        dialogo.setCancelable(false);
    }

    public void restablecerContraseña(View v) {
        String correo = etCorreo.getText().toString().trim();
        tilCorreo.setError(null);

        // Validar el campo de correo
        if (correo.isEmpty()) {
            tilCorreo.setError("Introduce un correo");
            return;
        } else if (!correo.matches(".+@.+[.].+")) {
            tilCorreo.setError("Introduce un correo válido");
            return;
        }

        // Mostrar ProgressDialog mientras se verifica el correo en Firestore
        dialogo.show();

        // Verificar si el correo existe en Firestore
        firestore.collection("usuarios")
                .whereEqualTo("correo", correo)
                .get()
                .addOnCompleteListener(task -> {
                    dialogo.dismiss();
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            // El correo existe, enviar enlace de restablecimiento
                            enviarEnlaceRestablecimiento(correo);
                        } else {
                            // El correo no está registrado, mostrar mensaje de error
                            tilCorreo.setError("Este correo no está registrado.");
                        }
                    } else {
                        // Error al consultar Firestore
                        Toast.makeText(ReestablecerContraseñaActivity.this, "Error al verificar el correo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void enviarEnlaceRestablecimiento(String correo) {
        // Mostrar ProgressDialog mientras se envía el enlace de restablecimiento
        dialogo.setMessage("Enviando enlace de restablecimiento...");
        dialogo.show();

        // Enviar enlace de restablecimiento de contraseña
        auth.sendPasswordResetEmail(correo).addOnCompleteListener(task -> {
            dialogo.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(ReestablecerContraseñaActivity.this, "Enlace de restablecimiento enviado a tu correo.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ReestablecerContraseñaActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ReestablecerContraseñaActivity.this, "Error al enviar el enlace: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void inicioSesion(View v) {
        Intent intent = new Intent(ReestablecerContraseñaActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

