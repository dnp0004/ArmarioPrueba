package com.example.armariocamara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViewHolder> {

    private List<Viaje> viajes;
    private OnViajeClickListener listener;

    public interface OnViajeClickListener {
        void onViajeClick(Viaje viaje);
    }

    public ViajesAdapter(List<Viaje> viajes, OnViajeClickListener listener) {
        this.viajes = viajes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_viaje, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Viaje viaje = viajes.get(position);

        holder.txtNombre.setText(viaje.nombre);
        holder.txtDestino.setText("📍 " + viaje.destino);
        holder.txtDias.setText(viaje.numDias + " días");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViajeClick(viaje);
            }
        });
    }

    @Override
    public int getItemCount() {
        return viajes != null ? viajes.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtDestino, txtDias;

        ViewHolder(View view) {
            super(view);
            txtNombre = view.findViewById(R.id.txtNombreViaje);
            txtDestino = view.findViewById(R.id.txtDestinoViaje);
            txtDias = view.findViewById(R.id.txtDiasViaje);
        }
    }
}