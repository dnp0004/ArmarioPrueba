package com.example.armariocamara;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para la lista de outfits favoritos
 * CORRIGE EL CRASH en MisLooksActivity
 */
public class LooksAdapter extends RecyclerView.Adapter<LooksAdapter.ViewHolder> {

    private Context context;
    private List<OutfitFavorito> listaFavoritos;
    private OnLookClickListener listener;

    public interface OnLookClickListener {
        void onLookClick(OutfitFavorito look);
    }

    public LooksAdapter(List<OutfitFavorito> lista, OnLookClickListener listener) {
        this.listaFavoritos = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_look_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OutfitFavorito look = listaFavoritos.get(position);

        // Nombre del outfit
        holder.txtNombre.setText(look.nombre != null ? look.nombre : "Outfit sin nombre");

        // Estilo
        holder.txtEstilo.setText(look.estilo != null ? look.estilo : "");

        // Fecha
        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(look.fechaCreacion));
        holder.txtFecha.setText(fecha);

        // Cargar imágenes de las prendas
        cargarImagenPrenda(look.idTop, holder.imgTop);
        cargarImagenPrenda(look.idBottom, holder.imgBottom);
        cargarImagenPrenda(look.idShoes, holder.imgShoes);
        cargarImagenPrenda(look.idOuter, holder.imgOuter);

        // Click
        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLookClick(look);
            }
        });
    }

    private void cargarImagenPrenda(int idPrenda, ImageView imageView) {
        if (idPrenda == 0) {
            imageView.setVisibility(View.GONE);
            return;
        }

        imageView.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                Prenda prenda = COMPLETO_AppDatabase.getDb(context).prendaDao().obtenerPorId(idPrenda);

                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (prenda != null && prenda.rutaImagen != null) {
                        File imgFile = new File(prenda.rutaImagen);
                        if (imgFile.exists()) {
                            Glide.with(context)
                                    .load(imgFile)
                                    .centerCrop()
                                    .into(imageView);
                        } else {
                            imageView.setBackgroundColor(android.graphics.Color.DKGRAY);
                        }
                    } else {
                        imageView.setBackgroundColor(android.graphics.Color.DKGRAY);
                    }
                });
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    imageView.setBackgroundColor(android.graphics.Color.DKGRAY);
                });
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return listaFavoritos != null ? listaFavoritos.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView txtNombre, txtEstilo, txtFecha;
        ImageView imgTop, imgBottom, imgShoes, imgOuter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardLook);
            txtNombre = itemView.findViewById(R.id.txtNombreLook);
            txtEstilo = itemView.findViewById(R.id.txtEstiloLook);
            txtFecha = itemView.findViewById(R.id.txtFechaLook);
            imgTop = itemView.findViewById(R.id.imgTopMini);
            imgBottom = itemView.findViewById(R.id.imgBottomMini);
            imgShoes = itemView.findViewById(R.id.imgShoesMini);
            imgOuter = itemView.findViewById(R.id.imgOuterMini);
        }
    }
}