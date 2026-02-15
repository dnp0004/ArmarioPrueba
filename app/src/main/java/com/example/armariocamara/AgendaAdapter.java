package com.example.armariocamara;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar eventos/outfits planificados en el calendario
 */
public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.EventoViewHolder> {

    public interface OnEventoClickListener {
        void onEventoClick(OutfitPlanificado evento);
    }

    private List<OutfitPlanificado> eventos;
    private Context context;
    private OnEventoClickListener listener;
    private SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public AgendaAdapter(List<OutfitPlanificado> eventos, OnEventoClickListener listener) {
        this.eventos = eventos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_evento_calendario, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        OutfitPlanificado evento = eventos.get(position);
        holder.bind(evento);
    }

    @Override
    public int getItemCount() {
        return eventos != null ? eventos.size() : 0;
    }

    public void actualizarEventos(List<OutfitPlanificado> nuevosEventos) {
        this.eventos = nuevosEventos;
        notifyDataSetChanged();
    }

    class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtFecha, txtHora;
        ImageView imgPreview1, imgPreview2, imgPreview3;
        View viewEstado, viewFondo;

        EventoViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloEvento);
            txtFecha = itemView.findViewById(R.id.txtFechaEvento);
            txtHora = itemView.findViewById(R.id.txtHoraEvento);
            imgPreview1 = itemView.findViewById(R.id.imgPreview1);
            imgPreview2 = itemView.findViewById(R.id.imgPreview2);
            imgPreview3 = itemView.findViewById(R.id.imgPreview3);
            viewEstado = itemView.findViewById(R.id.viewEstado);
            viewFondo = itemView.findViewById(R.id.viewFondoEvento);
        }

        void bind(OutfitPlanificado evento) {
            // Título
            txtTitulo.setText(evento.titulo != null ? evento.titulo : "Sin título");

            // Fecha
            try {
                Date fecha = sdfFecha.parse(evento.fecha);
                SimpleDateFormat sdfDisplay = new SimpleDateFormat("EEEE, d 'de' MMMM",
                        new Locale("es", "ES"));
                txtFecha.setText(sdfDisplay.format(fecha));

                // Verificar si es hoy
                String hoy = sdfFecha.format(Calendar.getInstance().getTime());
                boolean esHoy = evento.fecha.equals(hoy);

                // CORRECCIÓN: Color visible para fecha de hoy
                if (esHoy) {
                    viewFondo.setBackgroundColor(Color.parseColor("#2196F3")); // Azul
                    txtFecha.setTextColor(Color.WHITE); // Blanco
                    txtTitulo.setTextColor(Color.WHITE);
                } else {
                    viewFondo.setBackgroundColor(Color.WHITE);
                    txtFecha.setTextColor(Color.BLACK);
                    txtTitulo.setTextColor(Color.BLACK);
                }
            } catch (Exception e) {
                txtFecha.setText(evento.fecha);
            }

            // Hora (si existe)
            if (txtHora != null) {
                if (evento.fechaCreacion > 0) {
                    SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    txtHora.setText(sdfHora.format(new Date(evento.fechaCreacion)));
                    txtHora.setVisibility(View.VISIBLE);
                } else {
                    txtHora.setVisibility(View.GONE);
                }
            }

            // Estado (puesto/no puesto)
            if (viewEstado != null) {
                if (evento.puesto) {
                    viewEstado.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
                } else {
                    viewEstado.setBackgroundColor(Color.parseColor("#FF9800")); // Naranja
                }
            }

            // Cargar previews de prendas
            cargarPreviewPrendas(evento);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventoClick(evento);
                }
            });
        }

        private void cargarPreviewPrendas(OutfitPlanificado evento) {
            // Ocultar todas las previews primero
            imgPreview1.setVisibility(View.GONE);
            imgPreview2.setVisibility(View.GONE);
            imgPreview3.setVisibility(View.GONE);

            // Cargar en un thread separado
            new Thread(() -> {
                try {
                    COMPLETO_PrendaDao dao = COMPLETO_AppDatabase.getDb(context).prendaDao();

                    // Preview 1: Top
                    if (evento.idTop != 0) {
                        Prenda top = dao.obtenerPorId(evento.idTop);
                        if (top != null && top.rutaImagen != null) {
                            cargarImagen(imgPreview1, top.rutaImagen);
                        }
                    }

                    // Preview 2: Bottom
                    if (evento.idBottom != 0) {
                        Prenda bottom = dao.obtenerPorId(evento.idBottom);
                        if (bottom != null && bottom.rutaImagen != null) {
                            cargarImagen(imgPreview2, bottom.rutaImagen);
                        }
                    }

                    // Preview 3: Shoes
                    if (evento.idShoes != 0) {
                        Prenda shoes = dao.obtenerPorId(evento.idShoes);
                        if (shoes != null && shoes.rutaImagen != null) {
                            cargarImagen(imgPreview3, shoes.rutaImagen);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private void cargarImagen(ImageView imageView, String ruta) {
            File file = new File(ruta);
            if (file.exists()) {
                ((CalendarioActivity) context).runOnUiThread(() -> {
                    Glide.with(context)
                            .load(file)
                            .centerCrop()
                            .into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                });
            }
        }
    }
}