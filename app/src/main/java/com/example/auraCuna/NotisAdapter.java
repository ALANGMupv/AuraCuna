package com.example.auraCuna;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotisAdapter extends RecyclerView.Adapter<NotisAdapter.NotisViewHolder> {

    private List<Notis> notisList;

    public NotisAdapter(List<Notis> notisList) {
        this.notisList = notisList;
    }

    // Clase interna para gestionar las vistas de cada ítem
    class NotisViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTime;

        NotisViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    @NonNull
    @Override
    public NotisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion, parent, false);
        return new NotisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotisViewHolder holder, int position) {
        Notis notis = notisList.get(position);
        holder.tvTitle.setText(notis.getTitle());
        holder.tvDescription.setText(notis.getDescription());
        holder.tvTime.setText(notis.getTime());
    }

    @Override
    public int getItemCount() {
        return notisList.size();
    }

    // Método para agregar una nueva notificación
    public void addNotis(Notis notis) {
        notisList.add(0, notis); // Agrega al inicio
        notifyItemInserted(0); // Notifica al RecyclerView
    }

    // Método para actualizar toda la lista
    public void updateNotisList(List<Notis> newNotisList) {
        notisList.clear();
        notisList.addAll(newNotisList);
        notifyDataSetChanged();
    }


}
