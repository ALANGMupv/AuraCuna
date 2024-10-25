package com.example.aura;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterPage extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextUsername; // Añadido username

    Button signUp;

    TextView signIn;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference; // Para la base de datos

    // Google
    ImageView googleAuth;
    // Twitter
    ImageView twitterAuth;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 20;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        editTextEmail = findViewById(R.id.emailEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        editTextConfirmPassword = findViewById(R.id.confirmPasswordEditText); // Inicializando el campo de confirmar contraseña
        editTextUsername = findViewById(R.id.usernameEditText); // Inicializando el campo de nombre de usuario
        signIn = findViewById(R.id.registerTextView);
        signUp = findViewById(R.id.botonInicioSesion);

        // Inicializando referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Google
        googleAuth = findViewById(R.id.btnGoogle); // Asegúrate de que el ID coincide con el de tu layout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        // Twitter (añadir la lógica adecuada según tu implementación de Twitter)
        twitterAuth = findViewById(R.id.btnTwitter); // Asegúrate de que el ID coincide con el de tu layout
        twitterAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterPage.this, TwitterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterPage.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, confirmPassword, username;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                confirmPassword = String.valueOf(editTextConfirmPassword.getText()); // Obtener el texto de confirmar contraseña
                username = String.valueOf(editTextUsername.getText()); // Obtener el nombre de usuario

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterPage.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterPage.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(confirmPassword)) { // Verificación para confirmar contraseña vacía
                    Toast.makeText(RegisterPage.this, "Enter Confirm Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) { // Verificar si las contraseñas coinciden
                    Toast.makeText(RegisterPage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(username)) { // Verificación para nombre de usuario vacío
                    Toast.makeText(RegisterPage.this, "Enter Username", Toast.LENGTH_SHORT).show();
                    return;
                }

                // CÓDIGO DE VERIFICACIÓN
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser newUser = firebaseAuth.getCurrentUser(); // Obtiene el nuevo usuario
                                    if (newUser != null) {
                                        // Guardar el nombre de usuario en la base de datos
                                        String userId = newUser.getUid(); // Obtener el ID del usuario
                                        databaseReference.child(userId).child("username").setValue(username)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(RegisterPage.this, "Username saved!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(RegisterPage.this, "Failed to save username.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                        newUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterPage.this, "Verification email sent! Please check your inbox.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(RegisterPage.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    Toast.makeText(RegisterPage.this, "Registration Successfull", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterPage.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(RegisterPage.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    // Google método
    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", user.getUid());
                            map.put("name", user.getDisplayName());
                            map.put("profile", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

                            databaseReference.child(user.getUid()).setValue(map);
                            Toast.makeText(RegisterPage.this, "Google Sign-In successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterPage.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterPage.this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
