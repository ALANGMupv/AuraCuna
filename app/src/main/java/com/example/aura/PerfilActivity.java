package com.example.aura;


import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;


import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EDITAR_DATOS = 1;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView fotoUsuario;
    private TextView tvNombre, tvApellido, tvCorreo;
    private Button editarDatosPersonales, cambioContrasenya;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        cambioContrasenya = findViewById(R.id.cambiar_contrasenya);
        editarDatosPersonales = findViewById(R.id.editar_datos);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fotoUsuario = findViewById(R.id.foto_usuario);
        tvNombre = findViewById(R.id.tv_nombre);
        tvApellido = findViewById(R.id.tv_apellido);
        tvCorreo = findViewById(R.id.tv_correo);

        cargarDatosUsuario();

        editarDatosPersonales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lanzarEditarDatosPersonales(view);
            }
        });

        cambioContrasenya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lanzarCambioContrasenya(view);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        cargarDatosUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail();
            String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;


            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.usuario_icono) //
                        .error(R.drawable.usuario_icono) //
                        .into(fotoUsuario);
            }


            db.collection("usuarios")
                    .whereEqualTo("correo", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            String nombre = document.getString("nombre");
                            String apellido = document.getString("apellido");
                            String correo = document.getString("correo");

                            // Mostrar los datos en los TextView
                            tvNombre.setText(nombre);
                            tvApellido.setText(apellido);
                            tvCorreo.setText(correo);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Si no hay usuario autenticado
            Toast.makeText(this, "No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
        }
    }
    public void regresar(View v) {

        Intent i = new Intent(this, ConfiguracionActivity.class);
        startActivity(i);
        finish();
    }

    public void lanzarEditarDatosPersonales(View view){
        Intent i = new Intent(this, EditarDatosPersonalesActivity.class);
        startActivity(i);
    }

    public void lanzarCambioContrasenya(View view){
        Intent i = new Intent(this, CambiarContrasenyaActivity.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDITAR_DATOS && resultCode == RESULT_OK) {
            // Recibir los datos actualizados
            String nombre = data.getStringExtra("nombre");
            String apellidos = data.getStringExtra("apellidos");

            // Actualizar los TextViews con los nuevos datos
            if (nombre != null && apellidos != null) {
                tvNombre.setText(nombre);
                tvApellido.setText(apellidos);
            }
        }
    }

}


