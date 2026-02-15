package com.example.armariocamara;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrendaAdapter extends RecyclerView.Adapter<PrendaAdapter.ViewHolder> {

    private Context context;
    private List<Prenda> prendas;
    private List<Prenda> prendasOriginal; // Para el filtro
    private OnPrendaClickListener listener;

    public interface OnPrendaClickListener {
        void onPrendaClick(Prenda prenda);
    }

    public PrendaAdapter(Context context, List<Prenda> prendas, OnPrendaClickListener listener) {
        this.context = context;
        this.prendas = prendas;
        this.prendasOriginal = new ArrayList<>(prendas);
        this.listener = listener;
    }

    /**
     * Método para filtrar prendas por texto de búsqueda
     */
    public void filtrar(String txtBuscar) {
        int longitud = txtBuscar.length();

        if (longitud == 0) {
            prendas.clear();
            prendas.addAll(prendasOriginal);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<Prenda> coleccion = prendasOriginal.stream()
                        .filter(i -> (i.subtipo != null && i.subtipo.toLowerCase().contains(txtBuscar.toLowerCase())) ||
                                (i.categoria != null && i.categoria.toLowerCase().contains(txtBuscar.toLowerCase())) ||
                                (i.colores != null && i.colores.toLowerCase().contains(txtBuscar.toLowerCase())) ||
                                (i.marca != null && i.marca.toLowerCase().contains(txtBuscar.toLowerCase())))
                        .collect(Collectors.toList());
                prendas.clear();
                prendas.addAll(coleccion);
            } else {
                // Versión compatible para Android antiguo
                prendas.clear();
                String busqueda = txtBuscar.toLowerCase();
                for (Prenda p : prendasOriginal) {
                    if ((p.subtipo != null && p.subtipo.toLowerCase().contains(busqueda)) ||
                            (p.categoria != null && p.categoria.toLowerCase().contains(busqueda)) ||
                            (p.colores != null && p.colores.toLowerCase().contains(busqueda)) ||
                            (p.marca != null && p.marca.toLowerCase().contains(busqueda))) {
                        prendas.add(p);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prenda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Prenda prenda = prendas.get(position);

            // Cargar imagen con Glide
            cargarImagen(holder.imgPrenda, prenda.rutaImagen);

            // Establecer textos
            holder.txtSubtipo.setText(prenda.subtipo != null ? prenda.subtipo : "Prenda");
            holder.txtCategoria.setText(prenda.categoria != null ? prenda.categoria : "");

            // Mostrar icono de favorito
            if (holder.iconoFavorito != null) {
                holder.iconoFavorito.setVisibility(prenda.esFavorito ? View.VISIBLE : View.GONE);
            }

            // Mostrar overlay de lavandería
            if (holder.overlay != null && holder.iconoLavanderia != null) {
                boolean enLavanderia = prenda.enLavanderia;
                holder.overlay.setVisibility(enLavanderia ? View.VISIBLE : View.GONE);
                holder.iconoLavanderia.setVisibility(enLavanderia ? View.VISIBLE : View.GONE);
            }

            // Listener de clic
            holder.card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPrendaClick(prenda);
                }
            });

        } catch (Exception e) {
            Log.e("PrendaAdapter", "Error binding item: " + e.getMessage());
        }
    }

    /**
     * Método centralizado para cargar imágenes con Glide
     */
    private void cargarImagen(ImageView imageView, String rutaImagen) {
        if (rutaImagen == null || rutaImagen.isEmpty()) {
            imageView.setBackgroundColor(Color.parseColor("#E0E0E0")); // Gray 300
            return;
        }

        try {
            File file = new File(rutaImagen);

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.color.gray_300) // Placeholder mientras carga
                    .error(R.color.gray_400); // Error si falla

            if (file.exists()) {
                // Cargar desde archivo
                Glide.with(context)
                        .load(file)
                        .apply(options)
                        .into(imageView);
            } else {
                // Intentar cargar como URI
                Glide.with(context)
                        .load(Uri.parse(rutaImagen))
                        .apply(options)
                        .into(imageView);
            }
        } catch (Exception e) {
            Log.e("PrendaAdapter", "Error loading image: " + e.getMessage());
            imageView.setBackgroundColor(Color.parseColor("#BDBDBD")); // Gray 400
        }
    }

    @Override
    public int getItemCount() {
        return prendas != null ? prendas.size() : 0;
    }

    /**
     * Método para actualizar la lista completa de prendas
     */
    public void actualizarLista(List<Prenda> nuevasPrendas) {
        this.prendas = nuevasPrendas;
        this.prendasOriginal = new ArrayList<>(nuevasPrendas);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView imgPrenda, iconoFavorito, iconoLavanderia;
        TextView txtSubtipo, txtCategoria;
        View overlay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardPrenda);
            imgPrenda = itemView.findViewById(R.id.imgPrenda);
            iconoFavorito = itemView.findViewById(R.id.iconoFavorito);
            iconoLavanderia = itemView.findViewById(R.id.iconoLavanderia);
            txtSubtipo = itemView.findViewById(R.id.txtSubtipoPrenda);
            txtCategoria = itemView.findViewById(R.id.txtCategoriaPrenda);
            overlay = itemView.findViewById(R.id.overlayLavanderia);
        }
    }
}
