package com.example.armariocamara;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ColorPickerDialog extends Dialog {

    public interface OnColorSelectedListener {
        void onColorSelected(String colorNombre, String colorHex);
    }

    private static final ColorItem[] COLORES = {
            new ColorItem("Negro", "#000000"),
            new ColorItem("Blanco", "#FFFFFF"),
            new ColorItem("Gris", "#808080"),
            new ColorItem("Gris Claro", "#D3D3D3"),
            new ColorItem("Rojo", "#FF0000"),
            new ColorItem("Rojo Oscuro", "#8B0000"),
            new ColorItem("Burdeos", "#800020"),
            new ColorItem("Rosa", "#FFC0CB"),
            new ColorItem("Azul", "#0000FF"),
            new ColorItem("Azul Marino", "#000080"),
            new ColorItem("Azul Cielo", "#87CEEB"),
            new ColorItem("Turquesa", "#40E0D0"),
            new ColorItem("Verde", "#00FF00"),
            new ColorItem("Verde Oscuro", "#006400"),
            new ColorItem("Verde Oliva", "#808000"),
            new ColorItem("Menta", "#98FF98"),
            new ColorItem("Amarillo", "#FFFF00"),
            new ColorItem("Dorado", "#FFD700"),
            new ColorItem("Beige", "#F5F5DC"),
            new ColorItem("Crema", "#FFFDD0"),
            new ColorItem("Naranja", "#FFA500"),
            new ColorItem("Coral", "#FF7F50"),
            new ColorItem("Melocotón", "#FFDAB9"),
            new ColorItem("Salmón", "#FA8072"),
            new ColorItem("Púrpura", "#800080"),
            new ColorItem("Violeta", "#EE82EE"),
            new ColorItem("Magenta", "#FF00FF"),
            new ColorItem("Lavanda", "#E6E6FA"),
            new ColorItem("Marrón", "#A52A2A"),
            new ColorItem("Marrón Claro", "#D2691E"),
            new ColorItem("Camel", "#C19A6B"),
            new ColorItem("Chocolate", "#D2691E")
    };

    private OnColorSelectedListener listener;
    private RecyclerView recyclerColores;
    private TextView txtTitulo;

    public ColorPickerDialog(@NonNull Context context, OnColorSelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_color_picker);

        // ✅ CORRECCIÓN: Usar 85% del ancho en lugar de MATCH_PARENT
        if (getWindow() != null) {
            int width = (int) (getContext().getResources()
                    .getDisplayMetrics().widthPixels * 0.85);
            getWindow().setLayout(
                    width,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        txtTitulo = findViewById(R.id.txtTituloColorPicker);
        recyclerColores = findViewById(R.id.recyclerColores);

        txtTitulo.setText("Selecciona un color");

        // Configurar RecyclerView con 4 columnas
        recyclerColores.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerColores.setAdapter(new ColorAdapter(getColoresList()));
    }

    private List<ColorItem> getColoresList() {
        List<ColorItem> lista = new ArrayList<>();
        for (ColorItem color : COLORES) {
            lista.add(color);
        }
        return lista;
    }

    private static class ColorItem {
        String nombre;
        String hex;

        ColorItem(String nombre, String hex) {
            this.nombre = nombre;
            this.hex = hex;
        }
    }

    private class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {
        private List<ColorItem> colores;

        ColorAdapter(List<ColorItem> colores) {
            this.colores = colores;
        }

        @NonNull
        @Override
        public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_color_picker, parent, false);
            return new ColorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
            ColorItem color = colores.get(position);
            holder.bind(color);
        }

        @Override
        public int getItemCount() {
            return colores.size();
        }

        class ColorViewHolder extends RecyclerView.ViewHolder {
            View colorCircle;
            TextView txtNombreColor;

            ColorViewHolder(View itemView) {
                super(itemView);
                colorCircle = itemView.findViewById(R.id.colorCircle);
                txtNombreColor = itemView.findViewById(R.id.txtNombreColor);
            }

            void bind(ColorItem color) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setColor(Color.parseColor(color.hex));

                // Añadir borde para colores claros
                if (esColorClaro(color.hex)) {
                    drawable.setStroke(2, Color.parseColor("#CCCCCC"));
                }

                colorCircle.setBackground(drawable);
                txtNombreColor.setText(color.nombre);

                // Click listener
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onColorSelected(color.nombre, color.hex);
                    }
                    dismiss();
                });
            }

            private boolean esColorClaro(String hex) {
                int color = Color.parseColor(hex);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                double luminance = (0.299 * red + 0.587 * green + 0.114 * blue);
                return luminance > 200;
            }
        }
    }
}