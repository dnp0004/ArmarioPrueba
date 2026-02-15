package com.example.armariocamara;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GuardarPrendaActivity extends AppCompatActivity {

    private String rutaImagenFinal;
    private ArrayList<Integer> estilosSeleccionados = new ArrayList<>();

    // Views
    private ImageView imgPreview;
    private Spinner spCategoria, spTalla, spColor1, spColor2;
    private Switch switchSucio;
    private Button btnEstilos, btnGuardar;
    private EditText etSubtipo, etMarca;
    private ArrayAdapter<String> adapterTallas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardar_prenda);

        // Inicializar vistas
        inicializarVistas();

        // Configurar spinners
        configurarSpinners();

        // Configurar botones
        btnEstilos.setOnClickListener(v -> mostrarDialogoEstilos());
        btnGuardar.setOnClickListener(v -> guardarPrenda());

        // Procesar imagen recibida
        procesarImagenRecibida();
    }

    private void inicializarVistas() {
        imgPreview = findViewById(R.id.imgPreview);
        spCategoria = findViewById(R.id.spinnerCategoria);
        spTalla = findViewById(R.id.spinnerTalla);
        spColor1 = findViewById(R.id.spinnerColor1);
        spColor2 = findViewById(R.id.spinnerColor2);
        switchSucio = findViewById(R.id.switchSucio);
        btnEstilos = findViewById(R.id.btnEstilos);
        etSubtipo = findViewById(R.id.inputSubtipo);
        etMarca = findViewById(R.id.inputMarca);
        btnGuardar = findViewById(R.id.btnGuardarFinal);
    }

    private void procesarImagenRecibida() {
        Intent intent = getIntent();

        // Verificar si viene desde cámara
        if (intent.hasExtra("DESDE_CAMARA") &&
                intent.getBooleanExtra("DESDE_CAMARA", false)) {
            String rutaImagen = intent.getStringExtra("RUTA_IMAGEN");
            if (rutaImagen != null) {
                procesarImagenDesdeCamara(rutaImagen);
            }
        }
        // Verificar si viene desde galería
        else if (intent.hasExtra("URI_IMAGEN")) {
            String uriString = intent.getStringExtra("URI_IMAGEN");
            if (uriString != null) {
                procesarImagenDesdeGaleria(uriString);
            }
        }
        // Compatibilidad con versión anterior
        else if (intent.hasExtra("ruta_imagen")) {
            String rutaImagen = intent.getStringExtra("ruta_imagen");
            if (rutaImagen != null) {
                procesarImagenDesdeCamara(rutaImagen);
            }
        }
    }

    private void procesarImagenDesdeCamara(String rutaImagen) {
        try {
            File archivoImagen = new File(rutaImagen);
            if (archivoImagen.exists()) {
                Glide.with(this)
                        .load(archivoImagen)
                        .centerCrop()
                        .into(imgPreview);

                rutaImagenFinal = rutaImagen;
            } else {
                Toast.makeText(this, "Error: no se encontró la imagen",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("GuardarPrenda", "Error procesando imagen desde cámara: " + e.getMessage());
            Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void procesarImagenDesdeGaleria(String uriString) {
        try {
            Uri uri = Uri.parse(uriString);

            // Mostrar preview
            Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(imgPreview);

            // Copiar imagen a directorio de la app
            File dirImagenes = new File(getExternalFilesDir(null), "prendas");
            if (!dirImagenes.exists()) {
                dirImagenes.mkdirs();
            }

            String nombreArchivo = "prenda_" + System.currentTimeMillis() + ".jpg";
            File archivoFinal = new File(dirImagenes, nombreArchivo);

            // Copiar y comprimir
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            FileOutputStream fos = new FileOutputStream(archivoFinal);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            inputStream.close();

            rutaImagenFinal = archivoFinal.getAbsolutePath();

        } catch (Exception e) {
            Log.e("GuardarPrenda", "Error procesando imagen desde galería: " + e.getMessage());
            Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void configurarSpinners() {
        // Adaptador personalizado para texto negro en fondo blanco
        class SpinnerAdapterPersonalizado extends ArrayAdapter<String> {
            public SpinnerAdapterPersonalizado(String[] items) {
                super(GuardarPrendaActivity.this,
                        android.R.layout.simple_spinner_item, items);
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.WHITE);
                tv.setPadding(20, 20, 20, 20);
                return view;
            }
        }

        // Configurar spinner de categorías
        spCategoria.setAdapter(new SpinnerAdapterPersonalizado(DatosRopa.CATEGORIAS));
        spColor1.setAdapter(new SpinnerAdapterPersonalizado(DatosRopa.COLORES));
        spColor2.setAdapter(new SpinnerAdapterPersonalizado(DatosRopa.COLORES));

        // Adaptador de tallas
        adapterTallas = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.WHITE);
                tv.setPadding(20, 20, 20, 20);
                return view;
            }
        };

        adapterTallas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTalla.setAdapter(adapterTallas);

        // Listener para cambio de categoría
        spCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null && view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.BLACK);
                }
                List<String> tallas = DatosRopa.obtenerTallasPorCategoria(
                        DatosRopa.CATEGORIAS[position]);
                adapterTallas.clear();
                adapterTallas.addAll(tallas);
                adapterTallas.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void mostrarDialogoEstilos() {
        boolean[] seleccionados = new boolean[DatosRopa.ESTILOS.length];
        for (int index : estilosSeleccionados) {
            if (index >= 0 && index < seleccionados.length) {
                seleccionados[index] = true;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona los estilos")
                .setMultiChoiceItems(DatosRopa.ESTILOS, seleccionados,
                        (dialog, which, isChecked) -> {
                            if (isChecked) {
                                if (!estilosSeleccionados.contains(which)) {
                                    estilosSeleccionados.add(which);
                                }
                            } else {
                                estilosSeleccionados.remove(Integer.valueOf(which));
                            }
                        })
                .setPositiveButton("OK", (dialog, which) -> {
                    String texto = estilosSeleccionados.isEmpty()
                            ? "Seleccionar Estilos..."
                            : estilosSeleccionados.size() + " estilos seleccionados";
                    btnEstilos.setText(texto);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarPrenda() {
        // Validar imagen
        if (rutaImagenFinal == null || rutaImagenFinal.isEmpty()) {
            Toast.makeText(this, "Error: no hay imagen para guardar",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener datos del formulario
        String categoria = spCategoria.getSelectedItem().toString();
        String talla = (spTalla.getSelectedItem() != null) ?
                spTalla.getSelectedItem().toString() : "Única";
        String color1 = spColor1.getSelectedItem().toString();
        String color2 = spColor2.getSelectedItem().toString();
        String colorFinal = color1.equals(color2) ? color1 : color1 + "," + color2;
        String subtipo = etSubtipo.getText().toString().trim();
        if (subtipo.isEmpty()) subtipo = categoria;
        String marca = etMarca.getText().toString().trim();
        if (marca.isEmpty()) marca = "Sin Marca";

        // Construir string de estilos
        StringBuilder sbEstilos = new StringBuilder();
        for (Integer index : estilosSeleccionados) {
            if (index >= 0 && index < DatosRopa.ESTILOS.length) {
                sbEstilos.append(DatosRopa.ESTILOS[index]).append(",");
            }
        }
        String estilos = sbEstilos.length() > 0 ? sbEstilos.toString() : "Casual Diario";

        // Crear objeto Prenda
        Prenda prenda = new Prenda(
                rutaImagenFinal,
                categoria,
                subtipo,
                talla,
                colorFinal,
                marca,
                estilos
        );
        prenda.nombreArchivo = new File(rutaImagenFinal).getName();
        prenda.enLavanderia = switchSucio.isChecked();

        // Guardar en base de datos
        new Thread(() -> {
            try {
                COMPLETO_AppDatabase.getDb(this).prendaDao().insertarPrenda(prenda);

                runOnUiThread(() -> {
                    Toast.makeText(this, "¡Prenda guardada con éxito!",
                            Toast.LENGTH_SHORT).show();

                    // MODIFICACIÓN: Ya no pregunta, vuelve directamente al inicio
                    Intent intent = new Intent(GuardarPrendaActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                Log.e("GuardarPrenda", "Error al guardar: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}