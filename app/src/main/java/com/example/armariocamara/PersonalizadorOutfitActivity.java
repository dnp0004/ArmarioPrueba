package com.example.armariocamara;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PersonalizadorOutfitActivity extends AppCompatActivity {

    private ImageView imgTop, imgBottom, imgShoes, imgOuter;
    private Button btnGuardar, btnCalendario;

    private int idTopActual = 0;
    private int idBottomActual = 0;
    private int idShoesActual = 0;
    private int idOuterActual = 0;

    private String ocasion = "Casual Diario";
    private AlertDialog dialogActual;

    // Usamos drawables estándar de Android para evitar errores de compilación por archivos faltantes
    private final int PLACEHOLDER = android.R.drawable.ic_menu_gallery;
    private final int ERROR_IMAGE = android.R.drawable.stat_notify_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalizador);

        inicializarVistas();

        if (getIntent().hasExtra("id_top")) {
            idTopActual = getIntent().getIntExtra("id_top", 0);
            idBottomActual = getIntent().getIntExtra("id_bottom", 0);
            idShoesActual = getIntent().getIntExtra("id_shoes", 0);
            idOuterActual = getIntent().getIntExtra("id_outer", 0);
            cargarOutfitInicial();
        }

        imgTop.setOnClickListener(v -> cambiarPrenda("top"));
        imgBottom.setOnClickListener(v -> cambiarPrenda("bottom"));
        imgShoes.setOnClickListener(v -> cambiarPrenda("shoes"));
        imgOuter.setOnClickListener(v -> cambiarPrenda("outer"));

        btnGuardar.setOnClickListener(v -> guardarOutfit());
        btnCalendario.setOnClickListener(v -> agendarOutfit());
    }

    private void inicializarVistas() {
        imgTop = findViewById(R.id.imgTopPersonalizador);
        imgBottom = findViewById(R.id.imgBottomPersonalizador);
        imgShoes = findViewById(R.id.imgShoesPersonalizador);
        imgOuter = findViewById(R.id.imgOuterPersonalizador);

        btnGuardar = findViewById(R.id.btnGuardarOutfitPersonalizado);
        btnCalendario = findViewById(R.id.btnCalendarioPersonalizado);

        actualizarVistas();
    }

    private void cargarOutfitInicial() {
        if (idTopActual != 0) cargarImagenPrenda(imgTop, idTopActual);
        if (idBottomActual != 0) cargarImagenPrenda(imgBottom, idBottomActual);
        if (idShoesActual != 0) cargarImagenPrenda(imgShoes, idShoesActual);
        if (idOuterActual != 0) cargarImagenPrenda(imgOuter, idOuterActual);
    }

    private void actualizarVistas() {
        boolean algoSeleccionado = (idTopActual != 0 || idBottomActual != 0 || idShoesActual != 0 || idOuterActual != 0);
        btnGuardar.setVisibility(algoSeleccionado ? View.VISIBLE : View.GONE);
        if (btnCalendario != null) {
            btnCalendario.setVisibility(algoSeleccionado ? View.VISIBLE : View.GONE);
        }
    }

    private void cambiarPrenda(String tipoPrenda) {
        new Thread(() -> {
            try {
                List<Prenda> prendas = obtenerPrendasPorTipo(tipoPrenda);

                if (prendas.isEmpty()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No tienes prendas de tipo: " + tipoPrenda,
                                    Toast.LENGTH_SHORT).show());
                    return;
                }

                runOnUiThread(() -> mostrarSelectorPrenda(prendas, tipoPrenda));

            } catch (Exception e) {
                Log.e("Personalizador", "Error: " + e.getMessage());
            }
        }).start();
    }

    private List<Prenda> obtenerPrendasPorTipo(String tipo) {
        List<Prenda> todas = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerLimpias();
        List<Prenda> filtradas = new ArrayList<>();

        for (Prenda p : todas) {
            boolean coincide = false;
            String cat = p.categoria.toLowerCase();

            switch (tipo) {
                case "top":
                    coincide = cat.contains("superior") || cat.contains("camiseta") ||
                            cat.contains("camisa") || cat.contains("blusa") ||
                            cat.contains("sudadera") || cat.contains("top");
                    break;
                case "bottom":
                    coincide = cat.contains("inferior") || cat.contains("pantalon") ||
                            cat.contains("falda") || cat.contains("short") ||
                            cat.contains("jean");
                    break;
                case "shoes":
                    coincide = cat.contains("calzado") || cat.contains("zapatillas") ||
                            cat.contains("zapato") || cat.contains("bota") ||
                            cat.contains("sandalia");
                    break;
                case "outer":
                    coincide = cat.contains("abrigo") || cat.contains("chaqueta") ||
                            cat.contains("cazadora");
                    break;
            }
            if (coincide) filtradas.add(p);
        }
        return filtradas;
    }

    private void mostrarSelectorPrenda(List<Prenda> prendas, String tipo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_selector_prenda_grid, null);
        GridView grid = dialogView.findViewById(R.id.gridPrendasSelector);

        if (grid != null) {
            ArrayAdapter<Prenda> adapter = new ArrayAdapter<Prenda>(this, R.layout.item_prenda_selector, prendas) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_prenda_selector, parent, false);
                    Prenda p = getItem(position);

                    ImageView img = convertView.findViewById(R.id.imgSelectorPrenda);
                    TextView txt = convertView.findViewById(R.id.txtNombreSelectorPrenda);

                    if (txt != null) txt.setText(p.subtipo);
                    if (img != null && p.rutaImagen != null) {
                        Glide.with(getContext()).load(new File(p.rutaImagen)).centerCrop().into(img);
                    }
                    return convertView;
                }
            };
            grid.setAdapter(adapter);

            grid.setOnItemClickListener((parent, view, position, id) -> {
                Prenda seleccionada = prendas.get(position);

                switch (tipo) {
                    case "top":
                        idTopActual = seleccionada.id;
                        cargarImagenPrenda(imgTop, idTopActual);
                        break;
                    case "bottom":
                        idBottomActual = seleccionada.id;
                        cargarImagenPrenda(imgBottom, idBottomActual);
                        break;
                    case "shoes":
                        idShoesActual = seleccionada.id;
                        cargarImagenPrenda(imgShoes, idShoesActual);
                        break;
                    case "outer":
                        idOuterActual = seleccionada.id;
                        cargarImagenPrenda(imgOuter, idOuterActual);
                        break;
                }

                actualizarVistas();
                if (dialogActual != null) dialogActual.dismiss();
            });
        }

        dialogActual = builder.setView(dialogView)
                .setTitle("Selecciona " + tipo)
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Quitar prenda", (d, w) -> {
                    switch (tipo) {
                        case "top":
                            idTopActual = 0;
                            imgTop.setImageResource(PLACEHOLDER);
                            break;
                        case "bottom":
                            idBottomActual = 0;
                            imgBottom.setImageResource(PLACEHOLDER);
                            break;
                        case "shoes":
                            idShoesActual = 0;
                            imgShoes.setImageResource(PLACEHOLDER);
                            break;
                        case "outer":
                            idOuterActual = 0;
                            imgOuter.setImageResource(PLACEHOLDER);
                            break;
                    }
                    actualizarVistas();
                })
                .create();

        dialogActual.show();
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
                                    .placeholder(PLACEHOLDER)
                                    .error(ERROR_IMAGE)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("Personalizador", "Error cargando imagen: " + e.getMessage());
            }
        }).start();
    }

    private void guardarOutfit() {
        EditText input = new EditText(this);
        input.setHint("Nombre del outfit");
        new AlertDialog.Builder(this)
                .setTitle("Guardar Outfit")
                .setView(input)
                .setPositiveButton("GUARDAR", (d, w) -> {
                    String nombre = input.getText().toString().trim();
                    if (nombre.isEmpty()) nombre = "Mi Outfit Personalizado";

                    OutfitFavorito favorito = new OutfitFavorito(nombre, "Personalizado", ocasion,
                            idTopActual, idBottomActual, idShoesActual, idOuterActual, System.currentTimeMillis());

                    new Thread(() -> {
                        COMPLETO_AppDatabase.getDb(this).prendaDao().guardarFavorito(favorito);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "✅ Guardado en Mis Looks", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void agendarOutfit() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            String fechaString = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);

            new Thread(() -> {
                OutfitPlanificado plan = new OutfitPlanificado(
                        fechaString,
                        "Outfit Personalizado",
                        idTopActual, idBottomActual, idShoesActual, idOuterActual,
                        System.currentTimeMillis(),
                        false
                );
                COMPLETO_AppDatabase.getDb(this).prendaDao().agendarOutfit(plan);
                runOnUiThread(() -> Toast.makeText(this, "📅 Agendado para " + fechaString, Toast.LENGTH_SHORT).show());
            }).start();

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}