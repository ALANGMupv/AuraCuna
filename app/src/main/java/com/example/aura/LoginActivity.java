package com.example.aura;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 123;
    GoogleSignInClient googleSignInClient;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String correo = "";
    private String contraseña = "";
    private ViewGroup contenedor;
    private EditText etCorreo, etContraseña;
    private TextInputLayout tilCorreo, tilContraseña;
    private ProgressDialog dialogo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etCorreo = findViewById(R.id.correo);
        etContraseña = findViewById(R.id.contraseña);
        tilCorreo = findViewById(R.id.til_correo);
        tilContraseña = findViewById(R.id.til_contraseña);
        contenedor = findViewById(R.id.contenedor);

        dialogo = new ProgressDialog(this);
        dialogo.setTitle("Verificando usuario");
        dialogo.setMessage("Por favor espere...");
        dialogo.setCancelable(false);

        // Verificar si el usuario ya está autenticado
        verificaSiUsuarioValidado();

        //Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void verificaSiUsuarioValidado() {
        if (auth.getCurrentUser() != null) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void inicioSesiónCorreo(View v) {
        if (verificaCampos()) {
            dialogo.show();
            auth.signInWithEmailAndPassword(correo, contraseña).addOnCompleteListener(this, task -> {
                dialogo.dismiss();
                if (task.isSuccessful()) {
                    FirebaseUser usuario = auth.getCurrentUser();
                    if (usuario != null && usuario.isEmailVerified()) {
                        // Si el correo está verificado, proceder al inicio de sesión
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        // Si el correo no está verificado, cerrar sesión y mostrar mensaje
                        auth.signOut();
                        mensaje("Verifique su correo electrónico antes de iniciar sesión.");
                    }
                } else {
                    manejarError(task.getException());
                }
            });
        }
    }

    public void registro(View v) {
        Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
        startActivity(intent);
        finish();
    }

    public void restContraseña(View v) {
        Intent intent = new Intent(LoginActivity.this, ReestablecerContraseñaActivity.class);
        startActivity(intent);
        finish();
    }

    private void manejarError(Exception e) {
        String mensaje;
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            mensaje = "Credenciales inválidas.";
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            mensaje = "El correo ya está registrado.";
        } else {
            mensaje = e.getLocalizedMessage();
        }
        mensaje(mensaje);
    }

    private void mensaje(String mensaje) {
        Snackbar.make(contenedor, mensaje, Snackbar.LENGTH_LONG).show();
    }

    public void autentificarGoogle(View v) {
        googleSignInClient.revokeAccess().addOnCompleteListener(this, task -> {
            // Después de revocar acceso, inicia el flujo de inicio de sesión con Google
            Intent i = googleSignInClient.getSignInIntent();
            startActivityForResult(i, RC_GOOGLE_SIGN_IN);
        });
    }

    @Override public void onActivityResult(int requestCode, int resultCode,
                                           Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                googleAuth(account.getIdToken());
            } catch (ApiException e) {
                mensaje("Error de autentificación con Google");
            }
        }
    }

    private void googleAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                // Verifica si el usuario ya existe en Firestore
                                verificaSiUsuarioExiste(user);
                            }
                        } else {
                            mensaje(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    // Método para verificar si el usuario ya existe en Firestore
    private void verificaSiUsuarioExiste(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("usuarios").document(user.getUid());

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // El usuario ya existe en Firestore, procede a validar
                        verificaSiUsuarioValidado();
                    } else {
                        // El usuario no existe, entonces lo registramos en Firestore
                        guardarUsuarioEnFirestore(user);
                    }
                } else {
                    mensaje(task.getException().getLocalizedMessage());
                }
            }
        });
    }

    // Método para guardar el usuario en Firestore
    private void guardarUsuarioEnFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtiene los datos del usuario
        String nombre = user.getDisplayName();
        String correo = user.getEmail();

        // Divide el nombre completo en nombre y apellido si es posible
        String[] nombreCompleto = nombre != null ? nombre.split(" ", 2) : new String[]{};
        String primerNombre = nombreCompleto.length > 0 ? nombreCompleto[0] : "";
        String apellido = nombreCompleto.length > 1 ? nombreCompleto[1] : "";

        // Crea un mapa con los datos del usuario
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombre", primerNombre);
        userData.put("apellido", apellido);
        userData.put("correo", correo);

        // Guarda el usuario en Firestore
        db.collection("usuarios").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    mensaje("Usuario registrado correctamente");
                    verificaSiUsuarioValidado();
                })
                .addOnFailureListener(e -> mensaje("Error al registrar usuario: " + e.getLocalizedMessage()));
    }






    private boolean verificaCampos() {
        correo = etCorreo.getText().toString();
        contraseña = etContraseña.getText().toString();
        tilCorreo.setError(null);
        tilContraseña.setError(null);

        if (correo.isEmpty()) {
            tilCorreo.setError("Introduce un correo");
        } else if (!correo.matches(".+@.+[.].+") && correo.length() < 3) { // Verifica si no es un correo
            tilCorreo.setError("Introduce un correo válido");
        } else if (contraseña.isEmpty()) {
            tilContraseña.setError("Introduce una contraseña");
        } else if (contraseña.length() < 6) {
            tilContraseña.setError("Ha de contener al menos 6 caracteres");
        } else if (!contraseña.matches(".*[0-9].*")) {
            tilContraseña.setError("Ha de contener un número");
        } else if (!contraseña.matches(".*[A-Z].*")) {
            tilContraseña.setError("Ha de contener una letra mayúscula");
        } else {
            return true;
        }
        return false;
    }

}

















