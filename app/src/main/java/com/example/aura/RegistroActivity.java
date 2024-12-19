package com.example.aura;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
    private EditText etCorreo, etContraseña;
    private EditText etRepContraseña, etNombre, etApellido;

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
        etCorreo = findViewById(R.id.correo);
        etContraseña = findViewById(R.id.contraseña);
        etNombre = findViewById(R.id.nombre);
        etApellido = findViewById(R.id.apellido);
        etRepContraseña = findViewById(R.id.repContraseña);

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
                    // Obtener nombre y apellidos del GoogleSignInAccount
                    String nombreCompleto = account.getDisplayName();
                    String[] nombreApellidos = nombreCompleto != null ? nombreCompleto.split(" ", 2) : new String[]{"Nombre desconocido", "Apellido desconocido"};
                    String nombre = nombreApellidos[0];
                    String apellidos = nombreApellidos.length > 1 ? nombreApellidos[1] : "Apellido desconocido";

                    verificarRegistroEnFirestore(usuario, nombre, apellidos);
                }
            } else {
                dialogo.dismiss();
                Snackbar.make(findViewById(R.id.contenedor), "Error al autenticar con Firebase: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private void verificarRegistroEnFirestore(FirebaseUser usuario, String nombre, String apellidos) {
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
                // Usuario no registrado en Firestore, registrarlo con nombre y apellidos
                registrarUsuarioEnFirestore(usuario, nombre, apellidos);
            }
        }).addOnFailureListener(e -> {
            dialogo.dismiss();
            Snackbar.make(findViewById(R.id.contenedor), "Error al verificar el usuario en Firestore: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        });
    }

    private void registrarUsuarioEnFirestore(FirebaseUser usuario, String nombre, String apellidos) {
        String userId = usuario.getUid();
        String correo = usuario.getEmail();

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", nombre != null ? nombre : "Nombre desconocido");
        datosUsuario.put("apellido", apellidos != null ? apellidos : "Apellido desconocido");
        datosUsuario.put("correo", correo);
        datosUsuario.put("cunaAsociada", "cuna1");

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

    public void verificaciónRegistro(View view) {
        dialogo.show();
        String correo = etCorreo.getText().toString().trim();
        String contraseña = etContraseña.getText().toString().trim();
        String repContraseña = etRepContraseña.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellido.getText().toString().trim();

        // Verificaciones de los campos
        if (correo.isEmpty()) {
            etCorreo.setError("Introduce un correo");
            Toast.makeText(this, "Introduce un correo", Toast.LENGTH_SHORT).show();
            dialogo.dismiss();
            return;
        } else if (!correo.matches(".+@.+[.].+")) {
            etCorreo.setError("Introduce un correo válido");
            Toast.makeText(this, "Introduce un correo válido", Toast.LENGTH_SHORT).show();
            dialogo.dismiss();
            return;
        } else if (contraseña.isEmpty()) {
            etContraseña.setError("Introduce una contraseña");
            Toast.makeText(this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
            dialogo.dismiss();
            return;
        } else if (contraseña.length() < 6) {
            etContraseña.setError("Ha de contener al menos 6 caracteres");
            Toast.makeText(this, "Ha de contener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            dialogo.dismiss();
            return;
        } else if (!contraseña.matches(".*[0-9].*")) {
            etContraseña.setError("Ha de contener un número");
            Toast.makeText(this, "Ha de contener un número", Toast.LENGTH_SHORT).show();
            dialogo.dismiss();
            return;
        } else if (!contraseña.matches(".*[A-Z].*")) {
            etContraseña.setError("Ha de contener una letra mayúscula");
            Toast.makeText(this, "Ha de contener una letra mayúscula", Toast.LENGTH_SHORT).show();

            dialogo.dismiss();
            return;
        } else if (!contraseña.equals(repContraseña)) {
            etRepContraseña.setError("Las contraseñas no coinciden");
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            dialogo.dismiss();
            return;
        } else {
            auth.createUserWithEmailAndPassword(correo, contraseña).addOnCompleteListener(this, task -> {
                dialogo.dismiss();
                if (task.isSuccessful()) {
                    FirebaseUser usuario = auth.getCurrentUser();
                    if (usuario != null) {
                        // Llamada al método registrarUsuarioEnFirestore
                        registrarUsuarioEnFirestore(usuario, nombre, apellidos);

                        // Llamada al método enviarCorreoVerificacion
                        usuario.sendEmailVerification().addOnCompleteListener(emailTask -> {
                            if (emailTask.isSuccessful()) {
                                Snackbar.make(findViewById(R.id.contenedor), "Registro exitoso. Verifique su correo electrónico para activar su cuenta.", Snackbar.LENGTH_LONG).show();
                                // Redirige al LoginActivity
                                Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                // Cierra la sesión después del registro
                                auth.signOut();

                            } else {
                                Snackbar.make(findViewById(R.id.contenedor), "Error al enviar correo de verificación: " + emailTask.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    Snackbar.make(findViewById(R.id.contenedor), "Error al crear cuenta: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    public void ir_a_inicio_sesion(View v) {
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}




