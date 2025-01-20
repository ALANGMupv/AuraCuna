package com.example.auraCuna.casos_uso;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TwitterActivity extends HomePage {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        // Comprobar si hay un intento de autenticación pendiente
        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();

        if (pendingResultTask != null) {
            // Finalizar el inicio de sesión si hay un intento pendiente
            pendingResultTask
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            handleTwitterLoginSuccess(authResult);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showErrorMessage(e.getMessage());
                        }
                    });
        } else {
            // Configurar el proveedor de Twitter para autenticación
            OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
            provider.addCustomParameter("lang", "en"); // Idioma opcional

            firebaseAuth.startActivityForSignInWithProvider(/* activity= */ this, provider.build())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            handleTwitterLoginSuccess(authResult);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            vuelveAtras();
                            showErrorMessage(e.getMessage());
                        }
                    });
        }
    }

    private void handleTwitterLoginSuccess(AuthResult authResult) {
        FirebaseUser usuario = firebaseAuth.getCurrentUser();
        if (usuario != null) {
            // Obtener datos del usuario
            String displayName = usuario.getDisplayName(); // Nombre completo (de Twitter)
            String email = usuario.getEmail(); // Correo electrónico (puede ser null)
            String[] nombreApellidos = displayName != null ? displayName.split(" ", 2) : new String[]{"Nombre desconocido", "Apellido desconocido"};
            String nombre = nombreApellidos[0];
            String apellidos = nombreApellidos.length > 1 ? nombreApellidos[1] : "Apellido desconocido";

            // Registrar el usuario en Firestore
            registrarUsuarioEnFirestore(usuario, nombre, apellidos);

            // Redirigir a la página de inicio
            navigateToHomePage();
        }
    }

    private void registrarUsuarioEnFirestore(FirebaseUser usuario, String nombre, String apellidos) {
        String userId = usuario.getUid();
        String correo = usuario.getEmail();

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", nombre != null ? nombre : "Nombre desconocido");
        datosUsuario.put("apellido", apellidos != null ? apellidos : "Apellido desconocido");
        datosUsuario.put("correo", correo != null ? correo : "Correo no disponible");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(userId).set(datosUsuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TwitterActivity.this, "Usuario registrado exitosamente en Firestore.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TwitterActivity.this, "Error al registrar usuario en Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToHomePage() {
        startActivity(new Intent(TwitterActivity.this, HomePage.class));
        Toast.makeText(TwitterActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
        finish(); // Finalizar actividad actual para evitar volver atrás
    }

    private void vuelveAtras() {
        Intent intent = new Intent(TwitterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(TwitterActivity.this, "No se ha iniciado sesión correctamente con Twitter", Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(TwitterActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
    }
}
