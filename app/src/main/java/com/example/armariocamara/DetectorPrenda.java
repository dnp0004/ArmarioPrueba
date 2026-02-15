package com.example.armariocamara;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import java.io.IOException;
import java.util.List;

public class DetectorPrenda {

    public interface OnAnalisisTerminado {
        void alDetectar(String categoriaSugerida, String colorSugerido);
    }

    public static void analizarImagen(Context context, Uri imagenUri, OnAnalisisTerminado callback) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imagenUri);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            // Usamos el modelo base de Google
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        String cat = "Parte Superior"; // Por defecto
                        String col = "Negro"; // Por defecto

                        // Buscamos palabras clave en inglés (ML Kit suele devolver inglés)
                        for (ImageLabel label : labels) {
                            String text = label.getText().toLowerCase();
                            float confidence = label.getConfidence();

                            if (confidence > 0.7) { // Solo si está seguro al 70%
                                // Categoría
                                if (text.contains("shoe") || text.contains("sneaker") || text.contains("boot")) cat = "Zapatos";
                                else if (text.contains("pants") || text.contains("jeans") || text.contains("trousers") || text.contains("shorts")) cat = "Parte Inferior";
                                else if (text.contains("shirt") || text.contains("top") || text.contains("sweater") || text.contains("hoodie")) cat = "Parte Superior";
                                else if (text.contains("jacket") || text.contains("coat") || text.contains("blazer")) cat = "Abrigo";
                                else if (text.contains("dress") || text.contains("gown")) cat = "Cuerpo Entero";
                                else if (text.contains("hat") || text.contains("glasses") || text.contains("bag")) cat = "Accesorio";

                                // Color (Básico - ML Kit base no es experto en color, pero a veces acierta)
                                // Nota: Para color real se usa Palette API (que ya tienes), aquí solo reforzamos.
                            }
                        }
                        callback.alDetectar(cat, col);
                    })
                    .addOnFailureListener(e -> {
                        // Si falla, no hacemos nada
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}