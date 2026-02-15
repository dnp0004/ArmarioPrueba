package com.example.armariocamara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions;
import java.util.List;

public class ClasificadorIA {
    private final Context context;
    private ImageClassifier imageClassifier;

    public ClasificadorIA(Context context) {
        this.context = context;
        iniciarModelo();
    }

    private void iniciarModelo() {
        try {
            ImageClassifierOptions options = ImageClassifierOptions.builder().setMaxResults(1).build();
            // Asegúrate de que el archivo 'modelo_ropa.tflite' está en la carpeta assets
            imageClassifier = ImageClassifier.createFromFileAndOptions(context, "modelo_ropa.tflite", options);
        } catch (Exception e) {
            Log.e("IA", "Error iniciando modelo: " + e.getMessage());
        }
    }

    public String clasificarImagen(String rutaFoto) {
        if (imageClassifier == null) return "Desconocido";
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(rutaFoto);
            if (bitmap == null) return "Error Foto";

            TensorImage imagenTensor = TensorImage.fromBitmap(bitmap);
            List<Classifications> resultados = imageClassifier.classify(imagenTensor);

            if (resultados != null && !resultados.isEmpty()) {
                // Devuelve la etiqueta más probable (en inglés)
                return resultados.get(0).getCategories().get(0).getLabel();
            }
        } catch (Exception e) {
            Log.e("IA", "Error clasificando: " + e.getMessage());
        }
        return "Desconocido";
    }
}