package com.example.armariocamara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_GALLERY_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int REQUEST_IMAGE_PICK = 103;

    private Uri fotoUri;
    private String rutaFotoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar cards
        CardView cardAñadir = findViewById(R.id.cardCamara);
        CardView cardArmario = findViewById(R.id.cardArmario);
        CardView cardLooks = findViewById(R.id.cardLooks);
        CardView cardCalendario = findViewById(R.id.cardCalendario);
        CardView cardViajes = findViewById(R.id.cardViajes);
        CardView cardEstadisticas = findViewById(R.id.cardEstadisticas);
        CardView cardGenerarRapido = findViewById(R.id.cardGenerarRapido);

        // Configurar listeners
        cardAñadir.setOnClickListener(v -> mostrarOpcionesCamara());
        cardArmario.setOnClickListener(v -> startActivity(new Intent(this, ArmarioActivity.class)));
        cardLooks.setOnClickListener(v -> startActivity(new Intent(this, MisLooksActivity.class)));
        cardCalendario.setOnClickListener(v -> startActivity(new Intent(this, CalendarioActivity.class)));
        cardViajes.setOnClickListener(v -> startActivity(new Intent(this, ViajesPlannerActivity.class)));
        cardEstadisticas.setOnClickListener(v -> startActivity(new Intent(this, EstadisticasActivity.class)));

        // Listener para Generador Rápido
        cardGenerarRapido.setOnClickListener(v -> generarOutfitRapido());
    }

    private void mostrarOpcionesCamara() {
        String[] opciones = {"📷 Cámara", "🖼️ Galería"};
        new AlertDialog.Builder(this)
                .setTitle("Añadir Prenda")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        verificarPermisoCamara();
                    } else {
                        verificarPermisoGaleria();
                    }
                })
                .show();
    }

    // ==================== PERMISOS ====================
    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                mostrarDialogoPermisos("Cámara");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        } else {
            abrirCamara();
        }
    }

    /**
     * Verifica permisos de galería según la versión de Android
     * Android 13+ usa READ_MEDIA_IMAGES
     * Android 12- usa READ_EXTERNAL_STORAGE
     */
    private void verificarPermisoGaleria() {
        // Para Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                // Verificar si ya rechazó el permiso permanentemente
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_MEDIA_IMAGES)) {
                    mostrarDialogoPermisos("Galería (Fotos)");
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            REQUEST_GALLERY_PERMISSION);
                }
            } else {
                abrirGaleria();
            }
        }
        // Android 12 y anteriores
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    mostrarDialogoPermisos("Almacenamiento");
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_GALLERY_PERMISSION);
                }
            } else {
                abrirGaleria();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                mostrarDialogoPermisos("Cámara");
            }
        } else if (requestCode == REQUEST_GALLERY_PERMISSION) {
            // Verificamos si se concedió ALGUNO de los permisos solicitados
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                mostrarDialogoPermisos("Galería");
            }
        }
    }

    private void mostrarDialogoPermisos(String tipo) {
        new AlertDialog.Builder(this)
                .setTitle("Permiso Requerido")
                .setMessage("Necesitas activar el permiso de " + tipo +
                        " para usar esta función.\n\nVe a Ajustes > Permisos y activa el acceso.")
                .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ==================== CÁMARA ====================
    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File fotoFile = null;
            try {
                fotoFile = crearArchivoImagen();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de imagen",
                        Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Error creando archivo: " + ex.getMessage());
                return;
            }

            if (fotoFile != null) {
                fotoUri = FileProvider.getUriForFile(this,
                        "com.example.armariocamara.provider", fotoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "No se encontró app de cámara",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String nombreImagen = "PRENDA_" + timeStamp + "_";
        File dirImagenes = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Prendas");

        if (!dirImagenes.exists()) {
            dirImagenes.mkdirs();
        }

        File imagen = File.createTempFile(nombreImagen, ".jpg", dirImagenes);
        rutaFotoActual = imagen.getAbsolutePath();
        return imagen;
    }

    // ==================== GALERÍA ====================
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // ==================== RESULTADOS ====================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Foto desde cámara
                if (rutaFotoActual != null) {
                    Intent intent = new Intent(this, GuardarPrendaActivity.class);
                    intent.putExtra("RUTA_IMAGEN", rutaFotoActual);
                    intent.putExtra("DESDE_CAMARA", true);
                    startActivity(intent);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Foto desde galería
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    Intent intent = new Intent(this, GuardarPrendaActivity.class);
                    intent.putExtra("URI_IMAGEN", imageUri.toString());
                    intent.putExtra("DESDE_GALERIA", true);
                    startActivity(intent);
                }
            }
        }
    }

    // ==================== GENERADOR RÁPIDO ====================
    private void generarOutfitRapido() {
        Toast.makeText(this, "Generando outfit rápido...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                // Obtener todas las prendas
                List<Prenda> todas = COMPLETO_AppDatabase.getDb(this)
                        .prendaDao()
                        .obtenerTodasPrendas();

                if (todas == null || todas.isEmpty()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Añade prendas primero",
                                    Toast.LENGTH_SHORT).show());
                    return;
                }

                // Filtrar prendas limpias
                List<Prenda> limpias = new ArrayList<>();
                for (Prenda p : todas) {
                    if (!p.enLavanderia) limpias.add(p);
                }

                // Si no hay limpias, usamos todas (opcional: avisar al usuario)
                if (limpias.isEmpty()) limpias = todas;

                // Instancia del Estilista (se asume que existe la clase Estilista)
                Estilista estilista = new Estilista();

                // Generar outfit usando el método pro (simulado como aleatorio optimizado)
                // Parámetros: lista, ocasión default, sin restricciones, etc.
                List<Prenda> outfitLista = estilista.generarConjuntoProAvanzado(
                        limpias,
                        "Casual/Diario",
                        null,
                        new ArrayList<>(),
                        true, false, false, 3, 20, 4, false, new ArrayList<>(), false
                );

                if (outfitLista != null && !outfitLista.isEmpty()) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, VisualizadorOutfitActivity.class);

                        // Convertir lista a array de IDs para pasar al visualizador
                        int[] ids = new int[outfitLista.size()];
                        for(int i=0; i<outfitLista.size(); i++) {
                            ids[i] = outfitLista.get(i).id;
                        }

                        intent.putExtra("OUTFIT_IDS", ids);
                        intent.putExtra("TITULO", "Outfit Rápido");
                        startActivity(intent);
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se pudo generar un outfit completo. Añade más variedad de prendas.",
                                    Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                Log.e("MainActivity", "Error en generador rápido: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al generar outfit",
                                Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
