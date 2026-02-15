package com.example.armariocamara;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import java.util.*;

public class SelectorPrendaDialog extends Dialog {

    public interface OnPrendaSeleccionada {
        void onSeleccionada(Prenda prenda);
    }

    private final Context context;
    private final List<Prenda> todasLasPrendas;
    private final String categoriaFiltro;
    private final List<Prenda> excluir;
    private final OnPrendaSeleccionada callback;

    private RecyclerView recyclerPrendas;
    private SelectorAdapter adapter;
    private List<Prenda> prendasMostradas = new ArrayList<>();
    private TextView txtTituloSelector;
    private LinearLayout tabsContainer;

    private static final String[] SECCIONES = {
            "Todo", "Superior", "Inferior", "Cuerpo Entero",
            "Calzado", "Abrigo", "Accesorio", "Baño/Deporte"
    };

    public SelectorPrendaDialog(Context context, List<Prenda> prendas,
                                String categoriaFiltro, List<Prenda> excluir,
                                OnPrendaSeleccionada callback) {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.context = context;
        this.todasLasPrendas = prendas != null ? prendas : new ArrayList<>();
        this.categoriaFiltro = categoriaFiltro;
        this.excluir = excluir != null ? excluir : new ArrayList<>();
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_selector_prenda);

        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getWindow().setBackgroundDrawableResource(android.R.color.white);
        }

        txtTituloSelector = findViewById(R.id.txtTituloSelector);
        tabsContainer = findViewById(R.id.tabsSelector);
        recyclerPrendas = findViewById(R.id.recyclerSelector);
        ImageButton btnCerrar = findViewById(R.id.btnCerrarSelector);

        recyclerPrendas.setLayoutManager(new GridLayoutManager(context, 3));

        adapter = new SelectorAdapter(prendasMostradas, prenda -> {
            callback.onSeleccionada(prenda);
            dismiss();
        });
        recyclerPrendas.setAdapter(adapter);

        btnCerrar.setOnClickListener(v -> dismiss());

        if (categoriaFiltro != null) {
            txtTituloSelector.setText(obtenerTituloSeccion(categoriaFiltro));
            filtrarPor(categoriaFiltro);
            tabsContainer.setVisibility(View.GONE);
        } else {
            construirTabs();
            filtrarPor("Todo");
        }
    }

    private void construirTabs() {
        tabsContainer.removeAllViews();
        for (String seccion : SECCIONES) {
            TextView tab = new TextView(context);
            tab.setText(seccion);
            tab.setTextColor(Color.BLACK);
            tab.setBackgroundResource(R.drawable.tab_selector_bg);
            tab.setPadding(24, 12, 24, 12);
            tab.setTextSize(12f);
            tab.setSingleLine(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            tab.setLayoutParams(params);

            tab.setOnClickListener(v -> {
                actualizarTabActivo(tab);
                filtrarPor(seccion);
            });
            tabsContainer.addView(tab);
        }
    }

    private void actualizarTabActivo(TextView tabActivo) {
        for (int i = 0; i < tabsContainer.getChildCount(); i++) {
            View child = tabsContainer.getChildAt(i);
            if (child instanceof TextView) {
                boolean isActive = child == tabActivo;
                child.setBackgroundColor(isActive ?
                        Color.parseColor("#2196F3") : Color.parseColor("#F5F5F5"));
                ((TextView) child).setTextColor(isActive ? Color.WHITE : Color.BLACK);
            }
        }
    }

    private void filtrarPor(String seccion) {
        prendasMostradas.clear();
        Estilista estilista = new Estilista();

        for (Prenda p : todasLasPrendas) {
            if (excluir.stream().anyMatch(e -> e.id == p.id)) continue;

            boolean incluir = false;
            switch (seccion) {
                case "Todo":             incluir = true; break;
                case "Superior":         incluir = estilista.esCategoria(p, "superior"); break;
                case "Inferior":         incluir = estilista.esCategoria(p, "inferior"); break;
                case "Cuerpo Entero":    incluir = estilista.esCategoria(p, "cuerpo"); break;
                case "Calzado":          incluir = estilista.esCategoria(p, "zapatos"); break;
                case "Abrigo":           incluir = estilista.esCategoria(p, "abrigo"); break;
                case "Accesorio":        incluir = estilista.esCategoria(p, "accesorio"); break;
                case "Baño/Deporte":     incluir = estilista.esCategoria(p, "bano") || estilista.esCategoria(p, "deporte"); break;
                default:                 incluir = p.categoria != null && p.categoria.toLowerCase().contains(seccion.toLowerCase()); break;
            }
            if (incluir) prendasMostradas.add(p);
        }

        adapter.notifyDataSetChanged();

        if (prendasMostradas.isEmpty()) {
            txtTituloSelector.setText(obtenerTituloSeccion(seccion) + " — Sin prendas");
        } else {
            txtTituloSelector.setText(obtenerTituloSeccion(seccion) + " (" + prendasMostradas.size() + ")");
        }
    }

    private String obtenerTituloSeccion(String seccion) {
        switch (seccion) {
            case "superior": return "Parte Superior";
            case "inferior": return "Parte Inferior";
            case "cuerpo": return "Vestidos y Monos";
            case "zapatos": return "Calzado";
            case "abrigo": return "Abrigos y Chaquetas";
            case "accesorio": return "Accesorios";
            case "bano": return "Ropa de Baño";
            case "deporte": return "Ropa Deportiva";
            default: return seccion;
        }
    }

    public static class SelectorAdapter extends RecyclerView.Adapter<SelectorAdapter.ViewHolder> {

        private final List<Prenda> prendas;
        private final OnPrendaClick listener;

        interface OnPrendaClick { void onClick(Prenda p); }

        SelectorAdapter(List<Prenda> prendas, OnPrendaClick listener) {
            this.prendas = prendas;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_prenda_selector, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder h, int pos) {
            Prenda p = prendas.get(pos);

            h.txtNombre.setText(p.subtipo != null && !p.subtipo.isEmpty() ? p.subtipo : "Prenda");
            h.txtCategoria.setText(p.categoria != null ? obtenerCategoriaCort(p.categoria) : "");

            if (p.rutaImagen != null && !p.rutaImagen.isEmpty()) {
                File file = new File(p.rutaImagen);
                if (file.exists()) {
                    Glide.with(h.img.getContext())
                            .load(file)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(Color.parseColor("#F5F5F5"))
                            .into(h.img);
                } else {
                    try {
                        Glide.with(h.img.getContext())
                                .load(Uri.parse(p.rutaImagen))
                                .centerCrop()
                                .into(h.img);
                    } catch (Exception e) {
                        h.img.setBackgroundColor(Color.parseColor("#EEEEEE"));
                        h.img.setImageDrawable(null);
                    }
                }
            } else {
                h.img.setBackgroundColor(Color.parseColor("#EEEEEE"));
                h.img.setImageDrawable(null);
            }

            h.iconFav.setVisibility(p.esFavorito ? View.VISIBLE : View.GONE);

            h.card.setOnClickListener(v -> listener.onClick(p));
        }

        private static String obtenerCategoriaCort(String cat) {
            if (cat.contains(" - ")) {
                String[] parts = cat.split(" - ");
                return parts.length > 1 ? parts[1] : parts[0];
            }
            return cat.length() > 18 ? cat.substring(0, 18) + "…" : cat;
        }

        @Override
        public int getItemCount() { return prendas.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            androidx.cardview.widget.CardView card;
            ImageView img, iconFav;
            TextView txtNombre, txtCategoria;

            ViewHolder(View v) {
                super(v);
                card = v.findViewById(R.id.cardSelectorPrenda);
                img = v.findViewById(R.id.imgSelectorPrenda);
                txtNombre = v.findViewById(R.id.txtNombreSelectorPrenda);
                txtCategoria = v.findViewById(R.id.txtCategoriaSelectorPrenda);
                iconFav = v.findViewById(R.id.iconFavSelector);
            }
        }
    }
}