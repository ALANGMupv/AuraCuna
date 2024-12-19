package com.example.aura;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CambiarContrasenyaActivity extends AppCompatActivity {

    private Button cerrarBoton, confirmarBoton;
    private EditText etContraseñaActual, etNuevaContraseña, etRepNuevaContraseña;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasenya);

        // Inicializamos FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Ajustamos el tamaño de la ventana
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) (metrics.widthPixels * 0.9);
        int height = (int) (metrics.heightPixels * 0.75);
        getWindow().setLayout(width, height);

        // Referencias a los componentes de la vista
        cerrarBoton = findViewById(R.id.cancelar_cambio_contrasenya);
        confirmarBoton = findViewById(R.id.confirmar_cambio_contrasenya);
        etContraseñaActual = findViewById(R.id.et_contrasenya_vieja);
        etNuevaContraseña = findViewById(R.id.et_contrasenya_nueva);
        etRepNuevaContraseña = findViewById(R.id.et_contrasenya_nueva_rep);

        // Configuración del diálogo de progreso
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cambiando contraseña");
        progressDialog.setMessage("Por favor espere...");
        progressDialog.setCancelable(false);

        // Acción para el botón de cerrar
        cerrarBoton.setOnClickListener(v -> finish());

        // Acción para el botón de confirmar
        confirmarBoton.setOnClickListener(v -> cambiarContraseña());
    }

    private void cambiarContraseña() {
        String contraseñaActual = etContraseñaActual.getText().toString().trim();
        String nuevaContraseña = etNuevaContraseña.getText().toString().trim();
        String repNuevaContraseña = etRepNuevaContraseña.getText().toString().trim();

        // Validaciones
        if (contraseñaActual.isEmpty()) {
            etContraseñaActual.setError("Introduce la contraseña actual");
            return;
        }

        if (nuevaContraseña.isEmpty()) {
            etNuevaContraseña.setError("Introduce una nueva contraseña");
            return;
        }

        if (nuevaContraseña.length() < 6) {
            etNuevaContraseña.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!nuevaContraseña.matches(".*[0-9].*")) {
            etNuevaContraseña.setError("La contraseña debe contener al menos un número");
            return;
        }

        if (!nuevaContraseña.matches(".*[A-Z].*")) {
            etNuevaContraseña.setError("La contraseña debe contener al menos una letra mayúscula");
            return;
        }

        if (!nuevaContraseña.equals(repNuevaContraseña)) {
            etRepNuevaContraseña.setError("Las contraseñas no coinciden");
            return;
        }

        // Obtenemos el usuario actual
        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario != null) {
            progressDialog.show();

            // Reautenticar al usuario para actualizar la contraseña
            usuario.reauthenticate(EmailAuthProvider.getCredential(usuario.getEmail(), contraseñaActual))
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Si la autenticación es exitosa, actualizamos la contraseña
                            usuario.updatePassword(nuevaContraseña)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(CambiarContrasenyaActivity.this, "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(CambiarContrasenyaActivity.this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(CambiarContrasenyaActivity.this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
