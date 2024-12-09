package com.example.aura;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 123;
    private static final String TAG = "RegistroActivity";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private ProgressDialog dialogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Autenticando...");
        dialogo.setMessage("Por favor espere...");
        dialogo.setCancelable(false);

        // Configuración de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de que `default_web_client_id` esté configurado en `strings.xml`
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void autentificarGoogle(View view) {
        // Revoca el acceso de la cuenta previamente seleccionada
        googleSignInClient.revokeAccess().addOnCompleteListener(this, task -> {
            // Después de revocar acceso, inicia el flujo de inicio de sesión con Google
            Intent i = googleSignInClient.getSignInIntent();
            startActivityForResult(i, RC_GOOGLE_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            dialogo.show();
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    autenticarConFirebase(account);
                }
            } catch (ApiException e) {
                dialogo.dismiss();
                Snackbar.make(findViewById(R.id.contenedor), "Error al iniciar sesión con Google: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void autenticarConFirebase(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser usuario = auth.getCurrentUser();
                if (usuario != null) {
                    verificarRegistroEnFirestore(usuario);
                }
            } else {
                dialogo.dismiss();
                Snackbar.make(findViewById(R.id.contenedor), "Error al autenticar con Firebase: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void verificarRegistroEnFirestore(FirebaseUser usuario) {
        String correo = usuario.getEmail();
        if (correo == null) {
            dialogo.dismiss();
            Snackbar.make(findViewById(R.id.contenedor), "No se pudo obtener el correo del usuario.", Snackbar.LENGTH_LONG).show();
            return;
        }

        db.collection("usuarios").whereEqualTo("correo", correo).get().addOnCompleteListener(task -> {
            dialogo.dismiss();
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                // Usuario ya registrado en Firestore
                Snackbar.make(findViewById(R.id.contenedor), "Inicio de sesión exitoso.", Snackbar.LENGTH_LONG).show();
                redirigirAMainActivity();
            } else {
                // Usuario no registrado en Firestore, registrarlo
                registrarUsuarioEnFirestore(usuario);
            }
        }).addOnFailureListener(e -> {
            dialogo.dismiss();
            Snackbar.make(findViewById(R.id.contenedor), "Error al verificar el usuario en Firestore: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void registrarUsuarioEnFirestore(FirebaseUser usuario) {
        String userId = usuario.getUid();
        String correo = usuario.getEmail();
        String nombre = usuario.getDisplayName();

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", nombre != null ? nombre : "Nombre desconocido");
        datosUsuario.put("apellido", ""); // Puedes obtener más datos si es necesario
        datosUsuario.put("correo", correo);

        db.collection("usuarios").document(userId).set(datosUsuario).addOnSuccessListener(aVoid -> {
            Snackbar.make(findViewById(R.id.contenedor), "Usuario registrado exitosamente.", Snackbar.LENGTH_LONG).show();
            redirigirAMainActivity();
        }).addOnFailureListener(e -> Snackbar.make(findViewById(R.id.contenedor), "Error al registrar usuario: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    private void redirigirAMainActivity() {
        Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}




