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
import com.google.firebase.auth.OAuthProvider;

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
                            navigateToHomePage();
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
            provider.addCustomParameter("lang", "fr");

            firebaseAuth.startActivityForSignInWithProvider(/* activity= */ this, provider.build())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            navigateToHomePage();
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

    /**
     * Navega a la página de inicio solo si el inicio de sesión es exitoso.
     */
    private void navigateToHomePage() {
        startActivity(new Intent(TwitterActivity.this, HomePage.class));
        Toast.makeText(TwitterActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
        finish(); // Finalizar actividad actual para evitar volver atrás
    }

    /**
     * Navega a la página de inicio de sesión solo si el login no es exitoso.
     */
    private void vuelveAtras() {
        Intent intent = new Intent(TwitterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(TwitterActivity.this, "No se ha iniciado sesión correctamente con Twitter", Toast.LENGTH_SHORT).show();
    }

    /**
     * Muestra un mensaje de error si ocurre un problema durante la autenticación.
     */
    private void showErrorMessage(String message) {
        Toast.makeText(TwitterActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
    }
}
