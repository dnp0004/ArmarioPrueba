package com.example.armariocamara;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import java.util.*;

public class VisualizadorOutfitActivity extends AppCompatActivity {

    private Prenda pTop, pBottom, pShoes, pOuter;

    // Vistas
    private ImageView imgTop, imgBottom, imgShoes, imgOuter;
    private TextView txtTitulo;
    private Button btnGuardar, btnCalendario, btnCompartir, btnGenerarOtro;

    private String tituloOutfit = "Mi Outfit";
    private String ocasion = "Casual Diario";
    private AlertDialog dialogActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizador_outfit);

        vincularVistas();

        // Recibir datos del Intent
        int[] ids = getIntent().getIntArrayExtra("OUTFIT_IDS");
        if (ids == null) ids = getIntent().getIntArrayExtra("IDS_OUTFIT");

        tituloOutfit = getIntent().getStringExtra("TITULO");
        if (getIntent().hasExtra("OCASION")) {
            ocasion = getIntent().getStringExtra("OCASION");
        }

        if (tituloOutfit == null) tituloOutfit = "Mi Outfit";
        txtTitulo.setText(tituloOutfit);

        if (ids != null && ids.length > 0) {
            cargarPrendas(ids);
        }

        // Configurar Listeners
        btnGuardar.setOnClickListener(v -> guardarEnFavoritos());
        btnCalendario.setOnClickListener(v -> agendarOutfit());
        btnCompartir.setOnClickListener(v -> compartir());

        btnGenerarOtro.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioIAActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Tocar imágenes para cambiar prendas (Petición del usuario)
        imgTop.setOnClickListener(v -> cambiarPrenda("top"));
        imgBottom.setOnClickListener(v -> cambiarPrenda("bottom"));
        imgShoes.setOnClickListener(v -> cambiarPrenda("shoes"));
        imgOuter.setOnClickListener(v -> cambiarPrenda("outer"));
    }

    private void vincularVistas() {
        txtTitulo = findViewById(R.id.txtTituloVisualizador);
        btnGuardar = findViewById(R.id.btnGuardarVisualizador);
        btnCalendario = findViewById(R.id.btnCalendarioVisualizador);
        btnCompartir = findViewById(R.id.btnCompartirVisualizador);
        btnGenerarOtro = findViewById(R.id.btnGenerarOtro);

        imgTop = findViewById(R.id.imgTopVisualizador);
        imgBottom = findViewById(R.id.imgBottomVisualizador);
        imgShoes = findViewById(R.id.imgShoesVisualizador);
        imgOuter = findViewById(R.id.imgOuterVisualizador);
    }

    private void cargarPrendas(int[] ids) {
        new Thread(() -> {
            List<Prenda> prendas = new ArrayList<>();
            for (int id : ids) {
                if (id == 0) continue;
                Prenda p = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorId(id);
                if (p != null) prendas.add(p);
            }
            runOnUiThread(this::actualizarInterfaz); // Cambiado a expresión lambda
        }).start();
    }

    private void actualizarInterfaz() {
        if (pTop != null) {
            imgTop.setVisibility(View.VISIBLE);
            cargarImagenPrenda(imgTop, pTop.id);
        } else {
            imgTop.setImageResource(android.R.drawable.ic_menu_gallery);
            imgTop.setBackgroundColor(Color.parseColor("#F5F5F5"));
            Toast.makeText(this, "No hay prendas superiores disponibles", Toast.LENGTH_SHORT).show();
        }

        if (pBottom != null) {
            imgBottom.setVisibility(View.VISIBLE);
            cargarImagenPrenda(imgBottom, pBottom.id);
        } else {
            imgBottom.setImageResource(android.R.drawable.ic_menu_gallery);
            imgBottom.setBackgroundColor(Color.parseColor("#F5F5F5"));
            Toast.makeText(this, "No hay prendas inferiores disponibles", Toast.LENGTH_SHORT).show();
        }

        if (pShoes != null) {
            imgShoes.setVisibility(View.VISIBLE);
            cargarImagenPrenda(imgShoes, pShoes.id);
        } else {
            imgShoes.setImageResource(android.R.drawable.ic_menu_gallery);
            imgShoes.setBackgroundColor(Color.parseColor("#F5F5F5"));
            Toast.makeText(this, "No hay zapatos disponibles", Toast.LENGTH_SHORT).show();
        }

        if (pOuter != null) {
            imgOuter.setVisibility(View.VISIBLE);
            cargarImagenPrenda(imgOuter, pOuter.id);
        } else {
            imgOuter.setImageResource(android.R.drawable.ic_menu_gallery);
            imgOuter.setBackgroundColor(Color.parseColor("#F5F5F5"));
            Toast.makeText(this, "No hay abrigos disponibles", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarImagenPrenda(ImageView imageView, int prendaId) {
        new Thread(() -> {
            try {
                Prenda prenda = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorId(prendaId);
                if (prenda != null && prenda.rutaImagen != null) {
                    File file = new File(prenda.rutaImagen);
                    if (file.exists()) {
                        runOnUiThread(() -> {
                            Glide.with(this)
                                    .load(file)
                                    .placeholder(new ColorDrawable(Color.LTGRAY))
                                    .error(new ColorDrawable(Color.GRAY))
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("Visualizador", "Error cargando imagen: " + e.getMessage());
            }
        }).start();
    }

    // Funcionalidad para añadir/cambiar prendas en el "Outfit Rápido"
    private void cambiarPrenda(String tipo) {
        new Thread(() -> {
            List<Prenda> todas = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerLimpias();
            List<Prenda> filtradas = new ArrayList<>();
            Estilista estilista = new Estilista();

            for (Prenda p : todas) {
                if (tipo.equals("top") && (estilista.esCategoria(p, "superior") || estilista.esCategoria(p, "cuerpo"))) filtradas.add(p);
                else if (tipo.equals("bottom") && estilista.esCategoria(p, "inferior")) filtradas.add(p);
                else if (tipo.equals("shoes") && estilista.esCategoria(p, "zapatos")) filtradas.add(p);
                else if (tipo.equals("outer") && estilista.esCategoria(p, "abrigo")) filtradas.add(p);
            }

            runOnUiThread(() -> mostrarSelector(filtradas, tipo)); // Cambiado a expresión lambda
        }).start();
    }

    private void mostrarSelector(List<Prenda> prendas, String tipo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_selector_prenda_grid, null);
        GridView grid = dialogView.findViewById(R.id.gridPrendasSelector);
        EditText buscador = dialogView.findViewById(R.id.editBuscarPrenda);

        List<Prenda> prendasFiltradas = new ArrayList<>(prendas);
        ArrayAdapter<Prenda> adapter = new ArrayAdapter<>(this, R.layout.item_prenda_selector, prendasFiltradas) {
            @Override
            @NonNull
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null) view = LayoutInflater.from(getContext()).inflate(R.layout.item_prenda_selector, parent, false);
                Prenda p = getItem(position);
                if (p != null) {
                    ImageView img = view.findViewById(R.id.imgSelectorPrenda);
                    TextView txt = view.findViewById(R.id.txtNombreSelectorPrenda);
                    TextView txtColor = view.findViewById(R.id.txtColorSelectorPrenda);
                    TextView txtEstilo = view.findViewById(R.id.txtEstiloSelectorPrenda);
                    if (txt != null) txt.setText(p.subtipo);
                    if (txtColor != null) txtColor.setText(p.color);
                    if (txtEstilo != null) txtEstilo.setText(p.estilo);
                    if (img != null && p.rutaImagen != null) Glide.with(getContext()).load(new File(p.rutaImagen)).centerCrop().into(img);
                }
                return view;
            }
        };

        grid.setAdapter(adapter);

        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                prendasFiltradas.clear();
                for (Prenda p : prendas) {
                    if (p.subtipo.toLowerCase().contains(s.toString().toLowerCase()) ||
                            p.color.toLowerCase().contains(s.toString().toLowerCase()) ||
                            p.estilo.toLowerCase().contains(s.toString().toLowerCase())) {
                        prendasFiltradas.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        grid.setOnItemClickListener((parent, view, position, id) -> {
            Prenda seleccionada = prendasFiltradas.get(position);
            switch (tipo) {
                case "top": pTop = seleccionada; break;
                case "bottom": pBottom = seleccionada; break;
                case "shoes": pShoes = seleccionada; break;
                case "outer": pOuter = seleccionada; break;
            }
            actualizarInterfaz();
            Toast.makeText(this, "Prenda seleccionada: " + seleccionada.subtipo, Toast.LENGTH_SHORT).show();
            if (dialogActual != null) dialogActual.dismiss();
        });

        dialogActual = builder.setView(dialogView).setTitle("Seleccionar " + tipo).setNegativeButton("Cancelar", null).create();
        dialogActual.show();
    }

    private void guardarEnFavoritos() {
        if (pTop == null && pBottom == null && pShoes == null) {
            Toast.makeText(this, "Outfit incompleto", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setText(tituloOutfit);

        new AlertDialog.Builder(this)
                .setTitle("💾 Guardar en Favoritos")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    String nombre = input.getText().toString();
                    new Thread(() -> {
                        OutfitFavorito fav = new OutfitFavorito(nombre, "Personalizado", ocasion,
                                pTop != null ? pTop.id : 0,
                                pBottom != null ? pBottom.id : 0,
                                pShoes != null ? pShoes.id : 0,
                                pOuter != null ? pOuter.id : 0,
                                System.currentTimeMillis());
                        COMPLETO_AppDatabase.getDb(this).prendaDao().guardarFavorito(fav);
                        runOnUiThread(() -> Toast.makeText(this, "✅ Guardado en Mis Looks", Toast.LENGTH_SHORT).show());
                    }).start();
                }).show();
    }

    private void agendarOutfit() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            String fechaString = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            new Thread(() -> {
                OutfitPlanificado plan = new OutfitPlanificado(fechaString, tituloOutfit,
                        pTop != null ? pTop.id : 0, pBottom != null ? pBottom.id : 0,
                        pShoes != null ? pShoes.id : 0, pOuter != null ? pOuter.id : 0,
                        System.currentTimeMillis(), false);
                COMPLETO_AppDatabase.getDb(this).prendaDao().agendarOutfit(plan);
                runOnUiThread(() -> Toast.makeText(this, "📅 Agendado para " + fechaString, Toast.LENGTH_SHORT).show());
            }).start();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void compartir() {
        List<Prenda> lista = new ArrayList<>();
        if (pTop != null) lista.add(pTop);
        if (pBottom != null) lista.add(pBottom);
        if (pShoes != null) lista.add(pShoes);
        if (pOuter != null) lista.add(pOuter);
        if (lista.isEmpty()) {
            Toast.makeText(this, "Nada que compartir", Toast.LENGTH_SHORT).show();
            return;
        }
        CompartirUtils.compartirOutfitPro(this, lista, tituloOutfit);
    }
}