package com.example.aura;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TiendaAdaptador extends RecyclerView.Adapter<TiendaAdaptador.ViewHolder> {
    private List<Tienda> listaTiendas;

    public TiendaAdaptador(List<Tienda> listaTiendas) {
        this.listaTiendas = listaTiendas;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tienda, direccion;
        public ImageView foto;

        public ViewHolder(View itemView) {
            super(itemView);
            tienda = itemView.findViewById(R.id.tienda);
            direccion = itemView.findViewById(R.id.direccion);
            foto = itemView.findViewById(R.id.foto);
        }

        public void personaliza(Tienda tiendaObj) {
            tienda.setText(tiendaObj.getNombre());
            direccion.setText(tiendaObj.getUrlCompra());

            // Usamos Glide para cargar la imagen desde la URL
            Glide.with(itemView.getContext())
                    .load(tiendaObj.getFotoUrl())
                    .into(foto);

            // Configuramos el OnClickListener para abrir la URL en un navegador
            direccion.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(tiendaObj.getUrlCompra()));
                itemView.getContext().startActivity(intent);
            });


        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elementos_lista, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tienda tiendaObj = listaTiendas.get(position);
        holder.personaliza(tiendaObj);

        if (position%2 == 0){
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }
    }

    @Override
    public int getItemCount() {
        return listaTiendas.size();
    }
}
