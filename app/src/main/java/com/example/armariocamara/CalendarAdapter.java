package com.example.armariocamara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final List<OutfitPlanificado> eventos;
    private final OnItemListener onItemListener;

    public CalendarAdapter(List<OutfitPlanificado> eventos, OnItemListener onItemListener) {
        this.eventos = eventos;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        final OutfitPlanificado evento = eventos.get(position);
        holder.txtDiaMes.setText(evento.titulo);
        holder.itemView.setOnClickListener(v -> {
            if (onItemListener != null) {
                onItemListener.onItemClick(evento);
            }
        });
        holder.viewEventoPunto.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return eventos != null ? eventos.size() : 0;
    }

    public interface OnItemListener {
        void onItemClick(OutfitPlanificado evento);
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        public final TextView txtDiaMes;
        public final View viewEventoPunto;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDiaMes = itemView.findViewById(R.id.txtDiaMes);
            viewEventoPunto = itemView.findViewById(R.id.viewEventoPunto);
        }
    }
}
