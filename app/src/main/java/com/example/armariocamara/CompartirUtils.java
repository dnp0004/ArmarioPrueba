package com.example.armariocamara;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Utilidad para compartir outfits en redes sociales
 */
public class CompartirUtils {

    // ==========================================
    // MÉTODO AÑADIDO (El que faltaba)
    // ==========================================
    public static void compartirOutfitPro(Context context, List<Prenda> prendas, String titulo) {
        new Thread(() -> {
            try {
                if (prendas == null || prendas.isEmpty()) {
                    mostrarToast(context, "No hay prendas para compartir");
                    return;
                }

                // 1. Crear imagen collage usando la lista directa
                Bitmap collage = crearCollageOutfit(context, prendas, titulo);

                // 2. Guardar imagen temporal
                File imagenCompartir = guardarImagenTemp(context, collage);

                // 3. Compartir
                if (imagenCompartir != null) {
                    compartirImagen(context, imagenCompartir, titulo);
                } else {
                    mostrarToast(context, "Error al crear la imagen");
                }

            } catch (Exception e) {
                Log.e("CompartirUtils", "Error Pro: " + e.getMessage());
                mostrarToast(context, "Error al compartir");
            }
        }).start();
    }

    // ==========================================
    // MÉTODOS EXISTENTES (Optimizados)
    // ==========================================

    /**
     * Compartir un outfit guardado en BD (OutfitFavorito)
     */
    public static void compartirOutfit(Context context, OutfitFavorito outfit) {
        new Thread(() -> {
            try {
                COMPLETO_PrendaDao dao = COMPLETO_AppDatabase.getDb(context).prendaDao();
                List<Prenda> prendas = new ArrayList<>();

                if (outfit.idTop != 0) agregarSiExiste(dao, prendas, outfit.idTop);
                if (outfit.idBottom != 0) agregarSiExiste(dao, prendas, outfit.idBottom);
                if (outfit.idShoes != 0) agregarSiExiste(dao, prendas, outfit.idShoes);
                if (outfit.idOuter != 0) agregarSiExiste(dao, prendas, outfit.idOuter);

                if (prendas.isEmpty()) {
                    mostrarToast(context, "No hay prendas para compartir");
                    return;
                }

                Bitmap collage = crearCollageOutfit(context, prendas, outfit.nombre);
                File imagenCompartir = guardarImagenTemp(context, collage);

                if (imagenCompartir != null) {
                    compartirImagen(context, imagenCompartir, outfit.nombre);
                } else {
                    mostrarToast(context, "Error al crear imagen");
                }

            } catch (Exception e) {
                Log.e("CompartirUtils", "Error: " + e.getMessage());
                mostrarToast(context, "Error al compartir");
            }
        }).start();
    }

    private static void agregarSiExiste(COMPLETO_PrendaDao dao, List<Prenda> lista, int id) {
        Prenda p = dao.obtenerPorId(id);
        if (p != null) lista.add(p);
    }

    /**
     * Lógica principal para dibujar el collage
     */
    private static Bitmap crearCollageOutfit(Context context, List<Prenda> prendas, String nombreOutfit) {
        int cantidadPrendas = prendas.size();
        int anchoImagen = 1080;
        int altoImagen = cantidadPrendas <= 2 ? 1080 : 1350;

        Bitmap collage = Bitmap.createBitmap(anchoImagen, altoImagen, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(collage);

        // Fondo blanco
        canvas.drawColor(Color.WHITE);

        // Título
        Paint paintTexto = new Paint();
        paintTexto.setColor(Color.BLACK);
        paintTexto.setTextSize(60);
        paintTexto.setAntiAlias(true);
        paintTexto.setTextAlign(Paint.Align.CENTER);
        paintTexto.setFakeBoldText(true);

        canvas.drawText(nombreOutfit != null ? nombreOutfit : "Mi Outfit",
                anchoImagen / 2f, 80, paintTexto);

        // Línea decorativa
        Paint paintLinea = new Paint();
        paintLinea.setColor(Color.parseColor("#6200EE"));
        paintLinea.setStrokeWidth(5);
        canvas.drawLine(100, 120, anchoImagen - 100, 120, paintLinea);

        // Calcular layout
        int margen = 40;
        int inicioY = 160;
        int tamañoImagen;

        if (cantidadPrendas <= 2) {
            // Horizontal
            tamañoImagen = (anchoImagen - 3 * margen) / 2;
            for (int i = 0; i < prendas.size(); i++) {
                int x = margen + i * (tamañoImagen + margen);
                dibujarPrenda(context, canvas, prendas.get(i), x, inicioY, tamañoImagen);
            }
        } else {
            // Grid 2x2
            tamañoImagen = (anchoImagen - 3 * margen) / 2;
            for (int i = 0; i < prendas.size() && i < 4; i++) {
                int fila = i / 2;
                int columna = i % 2;
                int x = margen + columna * (tamañoImagen + margen);
                int y = inicioY + fila * (tamañoImagen + margen);
                dibujarPrenda(context, canvas, prendas.get(i), x, y, tamañoImagen);
            }
        }

        // Marca de agua
        paintTexto.setTextSize(30);
        paintTexto.setColor(Color.parseColor("#999999"));
        paintTexto.setFakeBoldText(false);
        canvas.drawText("Creado con Mi Armario App",
                anchoImagen / 2f, altoImagen - 40, paintTexto);

        return collage;
    }

    private static void dibujarPrenda(Context context, Canvas canvas, Prenda prenda,
                                      int x, int y, int tamaño) {
        try {
            if (prenda.rutaImagen == null) return;
            File file = new File(prenda.rutaImagen);
            if (!file.exists()) return;

            // Cargar Bitmap de forma síncrona (estamos en background thread)
            FutureTarget<Bitmap> futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .centerCrop() // Asegura que llene el cuadrado
                    .submit(tamaño, tamaño);

            Bitmap bitmap = futureTarget.get();

            // Dibujar borde
            Paint paintBorde = new Paint();
            paintBorde.setColor(Color.parseColor("#EEEEEE"));
            paintBorde.setStyle(Paint.Style.STROKE);
            paintBorde.setStrokeWidth(4);
            canvas.drawRect(x, y, x + tamaño, y + tamaño, paintBorde);

            // Dibujar imagen
            Rect destRect = new Rect(x, y, x + tamaño, y + tamaño);
            canvas.drawBitmap(bitmap, null, destRect, null);

            // Limpiar Glide
            Glide.with(context).clear(futureTarget);

        } catch (ExecutionException | InterruptedException e) {
            Log.e("CompartirUtils", "Error loading image: " + e.getMessage());
        }
    }

    private static File guardarImagenTemp(Context context, Bitmap bitmap) {
        try {
            File dirCache = new File(context.getCacheDir(), "shared");
            if (!dirCache.exists()) {
                dirCache.mkdirs();
            }

            // Nombre único
            File archivoImagen = new File(dirCache, "outfit_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(archivoImagen);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            return archivoImagen;
        } catch (Exception e) {
            Log.e("CompartirUtils", "Error saving image: " + e.getMessage());
            return null;
        }
    }

    private static void compartirImagen(Context context, File archivoImagen, String nombreOutfit) {
        // Volver al hilo principal para iniciar la actividad
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                try {
                    // Asegúrate de tener el Provider configurado en AndroidManifest.xml
                    Uri imageUri = FileProvider.getUriForFile(
                            context,
                            "com.example.armariocamara.provider", // DEBE COINCIDIR CON TU MANIFEST
                            archivoImagen
                    );

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/jpeg");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.putExtra(Intent.EXTRA_TEXT,
                            "¡Mira mi outfit \"" + (nombreOutfit != null ? nombreOutfit : "del día") +
                                    "\"! 👗✨ #MiArmario #OOTD");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    context.startActivity(Intent.createChooser(shareIntent, "Compartir outfit"));

                } catch (Exception e) {
                    Log.e("CompartirUtils", "Error sharing: " + e.getMessage());
                    Toast.makeText(context, "Error al abrir compartir", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void compartirPrenda(Context context, Prenda prenda) {
        try {
            File file = new File(prenda.rutaImagen);
            if (!file.exists()) {
                Toast.makeText(context, "Imagen no encontrada", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(
                    context,
                    "com.example.armariocamara.provider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    prenda.subtipo + " - " + prenda.marca + " #MiArmario");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Compartir prenda"));

        } catch (Exception e) {
            Log.e("CompartirUtils", "Error: " + e.getMessage());
            Toast.makeText(context, "Error al compartir", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper para mostrar Toast desde hilos background
    private static void mostrarToast(Context context, String mensaje) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            );
        }
    }
}