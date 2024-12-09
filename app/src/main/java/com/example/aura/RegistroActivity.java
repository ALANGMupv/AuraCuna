package com.example.aura;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText etCorreo, etContraseña, etRepContraseña, etNombre, etApellido;
    private TextInputLayout tilCorreo, tilContraseña, tilRepContraseña, tilNombre, tilApellido;
    private ProgressDialog dialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inicializar Firestore

        etCorreo = findViewById(R.id.correo);
        etContraseña = findViewById(R.id.contraseña);
        etRepContraseña = findViewById(R.id.repContraseña);
        etNombre = findViewById(R.id.nombre);
        etApellido = findViewById(R.id.apellido);

        tilCorreo = findViewById(R.id.til_correo);
        tilContraseña = findViewById(R.id.til_contraseña);
        tilRepContraseña = findViewById(R.id.til_repContraseña);
        tilNombre = findViewById(R.id.til_nombre);
        tilApellido = findViewById(R.id.til_apellido);

        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Registrando usuario");
        dialogo.setMessage("Por favor espere...");
        dialogo.setCancelable(false);
    }

    public void verificaciónRegistro(View view) {
        if (verificaCampos()) {
            dialogo.show();
            String correo = etCorreo.getText().toString().trim();
            String contraseña = etContraseña.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();

            auth.createUserWithEmailAndPassword(correo, contraseña).addOnCompleteListener(this, task -> {
                dialogo.dismiss();
                if (task.isSuccessful()) {
                    enviarCorreoVerificacion();
                    registrarUsuario(nombre, apellido, correo);

                    // Cierra la sesión después del registro
                    auth.signOut();

                    // Notifica al usuario que revise su correo y redirige al LoginActivity
                    Snackbar.make(findViewById(R.id.contenedor), "Registro exitoso. Verifique su correo electrónico para activar su cuenta.", Snackbar.LENGTH_LONG).show();
                    Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Snackbar.make(findViewById(R.id.contenedor), "Error al crear cuenta: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }


    private void enviarCorreoVerificacion() {
        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario != null) {
            usuario.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Correo de verificación enviado
                    Snackbar.make(findViewById(R.id.contenedor), "Correo de verificación enviado. Revise su bandeja de entrada.", Snackbar.LENGTH_LONG).show();
                } else {
                    // Error al enviar el correo de verificación
                    Snackbar.make(findViewById(R.id.contenedor), "Error al enviar el correo de verificación: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private void registrarUsuario(String nombre, String apellido, String correo) {
        // Obtener el ID de usuario de Firebase Authentication
        String userId = auth.getCurrentUser().getUid();

        // Guardar los datos del usuario en Firestore
        guardarUsuarioEnFirestore(userId, nombre, apellido, correo);
    }

    private void guardarUsuarioEnFirestore(String userId, String nombre, String apellido, String correo) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", nombre);
        usuario.put("apellido", apellido);
        usuario.put("correo", correo);

        db.collection("usuarios").document(userId)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    // Datos guardados con éxito, abrir MainActivity
                    Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Manejar el error al guardar en Firestore
                    Snackbar.make(findViewById(R.id.contenedor), "Error al guardar datos: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private boolean verificaCampos() {
        String correo = etCorreo.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String contraseña = etContraseña.getText().toString().trim();
        String repContraseña = etRepContraseña.getText().toString().trim();

        tilCorreo.setError(null);
        tilContraseña.setError(null);
        tilRepContraseña.setError(null);
        tilNombre.setError(null);
        tilApellido.setError(null);

        if (correo.isEmpty()) {
            tilCorreo.setError("Introduce un correo");
            return false;
        } else if (!correo.matches(".+@.+[.].+")) {
            tilCorreo.setError("Correo no válido");
            return false;
        } else if (nombre.isEmpty()) {
            tilNombre.setError("Introduce un nombre");
            return false;
        } else if (apellido.isEmpty()) {
            tilApellido.setError("Introduce un apellido");
            return false;
        } else if (contraseña.isEmpty()) {
            tilContraseña.setError("Introduce una contraseña");
            return false;
        } else if (contraseña.length() < 6) {
            tilContraseña.setError("Ha de contener al menos 6 caracteres");
            return false;
        } else if (!contraseña.matches(".*[0-9].*")) {
            tilContraseña.setError("Ha de contener un número");
            return false;
        } else if (!contraseña.matches(".*[A-Z].*")) {
            tilContraseña.setError("Ha de contener una letra mayúscula");
            return false;
        } else if (!contraseña.equals(repContraseña)) {
            tilRepContraseña.setError("Las contraseñas no coinciden");
            return false;
        }
        return true;
    }

    private void manejarError(Exception e) {
        String mensaje;
        if (e instanceof FirebaseAuthUserCollisionException) {
            mensaje = "El correo ya está registrado.";
        } else {
            mensaje = e.getLocalizedMessage();
        }
        Snackbar.make(findViewById(R.id.contenedor), mensaje, Snackbar.LENGTH_LONG).show();
    }

    public void inicioSesion(View v) {
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}



