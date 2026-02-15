package com.example.armariocamara;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditarPrendaActivity extends AppCompatActivity {

    // Constante para la selección de imagen
    private static final int REQUEST_IMAGE_PICK = 200;

    // Variable para almacenar la nueva imagen temporalmente
    private Uri nuevaImagenUri = null;

    private ImageView imgPreview;
    private EditText etSubtipo;
    private Spinner spinnerCategoria, spinnerColor1, spinnerColor2;
    private Button btnEstilos;
    private CheckBox checkFavorito, checkLavanderia;
    private Button btnGuardar, btnCancelar;
    private Prenda prenda;
    private int prendaId;
    private List<String> estilosSeleccionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_editar_simple);

        inicializarVistas();

        prendaId = getIntent().getIntExtra("prenda_id", -1);

        if (prendaId == -1) {
            Toast.makeText(this, "Error al cargar prenda", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarPrenda();

        // Listeners
        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnCancelar.setOnClickListener(v -> finish());
        btnEstilos.setOnClickListener(v -> mostrarDialogoEstilos());

        // Listener para cambiar la imagen al tocarla
        imgPreview.setOnClickListener(v -> cambiarImagen());
    }

    private void inicializarVistas() {
        imgPreview = findViewById(R.id.imgPrendaPreview);
        etSubtipo = findViewById(R.id.etSubtipoEditar);
        spinnerCategoria = findViewById(R.id.spinnerCategoriaEditar);
        spinnerColor1 = findViewById(R.id.spinnerColor1Editar);
        spinnerColor2 = findViewById(R.id.spinnerColor2Editar);
        btnEstilos = findViewById(R.id.btnEstilosEditar);
        checkFavorito = findViewById(R.id.checkFavoritoEditar);
        checkLavanderia = findViewById(R.id.checkLavanderiaEditar);
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        btnCancelar = findViewById(R.id.btnCancelarEdicion);

        // CONFIGURAR SPINNERS CON TEXTO NEGRO VISIBLE
        String[] categorias = {
                "Parte Superior", "Parte Inferior", "Calzado",
                "Abrigo/Chaqueta", "Vestido/Cuerpo Entero", "Accesorio"
        };

        ArrayAdapter<String> adapterCat = crearAdaptadorVisible(categorias);
        spinnerCategoria.setAdapter(adapterCat);

        ArrayAdapter<String> adapterColor = crearAdaptadorVisible(DatosRopa.COLORES);
        spinnerColor1.setAdapter(adapterColor);
        spinnerColor2.setAdapter(adapterColor);
    }

    private ArrayAdapter<String> crearAdaptadorVisible(String[] items) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(16);
                tv.setPadding(12, 12, 12, 12);
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextSize(14);
                tv.setPadding(16, 16, 16, 16);
                return v;
            }
        };
    }

    // MÉTODO PARA ABRIR GALERÍA
    private void cambiarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // MÉTODO PARA PROCESAR LA IMAGEN SELECCIONADA
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_PICK) {
            if (data != null && data.getData() != null) {
                nuevaImagenUri = data.getData();
                // Mostrar preview inmediato
                Glide.with(this)
                        .load(nuevaImagenUri)
                        .centerCrop()
                        .into(imgPreview);
            }
        }
    }

    private void cargarPrenda() {
        new Thread(() -> {
            prenda = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorId(prendaId);

            if (prenda == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Prenda no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            runOnUiThread(() -> {
                // CARGAR IMAGEN EXISTENTE
                if (prenda.rutaImagen != null && new File(prenda.rutaImagen).exists()) {
                    Glide.with(this)
                            .load(new File(prenda.rutaImagen))
                            .centerCrop()
                            .into(imgPreview);
                } else {
                    imgPreview.setBackgroundColor(Color.LTGRAY);
                }

                // RELLENAR CAMPOS
                etSubtipo.setText(prenda.subtipo != null ? prenda.subtipo : "");

                // Seleccionar categoría
                String categoria = prenda.categoria != null ? prenda.categoria.toLowerCase() : "";
                int posicion = 0;
                if (categoria.contains("superior")) posicion = 0;
                else if (categoria.contains("inferior")) posicion = 1;
                else if (categoria.contains("calzado") || categoria.contains("zapato")) posicion = 2;
                else if (categoria.contains("abrigo") || categoria.contains("chaqueta")) posicion = 3;
                else if (categoria.contains("vestido") || categoria.contains("cuerpo")) posicion = 4;
                else if (categoria.contains("accesorio")) posicion = 5;

                spinnerCategoria.setSelection(posicion);

                // Cargar colores
                if (prenda.colores != null && !prenda.colores.isEmpty()) {
                    String[] colores = prenda.colores.split(",");
                    if (colores.length > 0) {
                        int pos1 = buscarPosicionColor(colores[0].trim());
                        if (pos1 >= 0) spinnerColor1.setSelection(pos1);
                    }
                    if (colores.length > 1) {
                        int pos2 = buscarPosicionColor(colores[1].trim());
                        if (pos2 >= 0) spinnerColor2.setSelection(pos2);
                    }
                }

                // Cargar estilos
                if (prenda.estilos != null && !prenda.estilos.isEmpty()) {
                    estilosSeleccionados = new ArrayList<>();
                    // Manejar formato lista o string
                    if (prenda.estilos.contains("[") || prenda.estilos.contains(",")) {
                        String limpio = prenda.estilos.replace("[", "").replace("]", "");
                        String[] arr = limpio.split(",");
                        for(String s : arr) estilosSeleccionados.add(s.trim());
                    } else {
                        estilosSeleccionados.add(prenda.estilos);
                    }
                    btnEstilos.setText(estilosSeleccionados.size() + " estilos seleccionados");
                }

                checkFavorito.setChecked(prenda.esFavorito);
                checkLavanderia.setChecked(prenda.enLavanderia);
            });
        }).start();
    }

    private int buscarPosicionColor(String color) {
        for (int i = 0; i < DatosRopa.COLORES.length; i++) {
            if (DatosRopa.COLORES[i].equalsIgnoreCase(color)) {
                return i;
            }
        }
        return 0;
    }

    private void mostrarDialogoEstilos() {
        boolean[] seleccionados = new boolean[DatosRopa.ESTILOS.length];

        for (int i = 0; i < DatosRopa.ESTILOS.length; i++) {
            for (String estilo : estilosSeleccionados) {
                if (DatosRopa.ESTILOS[i].equals(estilo.trim())) {
                    seleccionados[i] = true;
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar Estilos")
                .setMultiChoiceItems(DatosRopa.ESTILOS, seleccionados, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!estilosSeleccionados.contains(DatosRopa.ESTILOS[which])) {
                            estilosSeleccionados.add(DatosRopa.ESTILOS[which]);
                        }
                    } else {
                        estilosSeleccionados.remove(DatosRopa.ESTILOS[which]);
                    }
                })
                .setPositiveButton("OK", (d, w) -> {
                    btnEstilos.setText(estilosSeleccionados.size() + " estilos seleccionados");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarCambios() {
        String subtipo = etSubtipo.getText().toString().trim();

        if (subtipo.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // GUARDAR NUEVA IMAGEN SI EXISTE
        if (nuevaImagenUri != null) {
            try {
                // Copiar la imagen a la carpeta privada de la app para persistencia
                InputStream inputStream = getContentResolver().openInputStream(nuevaImagenUri);
                File dirImagenes = new File(getExternalFilesDir(null), "prendas");
                if (!dirImagenes.exists()) {
                    dirImagenes.mkdirs();
                }

                String nombreArchivo = "edit_" + System.currentTimeMillis() + ".jpg";
                File archivoFinal = new File(dirImagenes, nombreArchivo);

                // Comprimir y guardar
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                FileOutputStream fos = new FileOutputStream(archivoFinal);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                if (inputStream != null) inputStream.close();

                // Actualizar la ruta en el objeto prenda
                prenda.rutaImagen = archivoFinal.getAbsolutePath();

            } catch (Exception e) {
                Log.e("EditarPrenda", "Error guardando nueva imagen: " + e.getMessage());
                Toast.makeText(this, "Error al guardar la nueva imagen", Toast.LENGTH_SHORT).show();
            }
        }

        prenda.subtipo = subtipo;
        prenda.categoria = spinnerCategoria.getSelectedItem().toString();

        String color1 = spinnerColor1.getSelectedItem().toString();
        String color2 = spinnerColor2.getSelectedItem().toString();

        // Formato limpio para colores
        if (color1.equals(color2)) {
            prenda.colores = color1;
        } else {
            prenda.colores = color1 + "," + color2;
        }

        // Formato para estilos (separados por coma)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < estilosSeleccionados.size(); i++) {
            sb.append(estilosSeleccionados.get(i));
            if (i < estilosSeleccionados.size() - 1) sb.append(",");
        }
        prenda.estilos = sb.toString();

        prenda.esFavorito = checkFavorito.isChecked();
        prenda.enLavanderia = checkLavanderia.isChecked();

        new Thread(() -> {
            COMPLETO_AppDatabase.getDb(this).prendaDao().actualizar(prenda);

            runOnUiThread(() -> {
                Toast.makeText(this, "✅ Cambios guardados", Toast.LENGTH_SHORT).show();

                // Devolver resultado OK para actualizar lista anterior si es necesario
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }
}