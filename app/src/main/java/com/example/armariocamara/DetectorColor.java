package com.example.armariocamara;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.palette.graphics.Palette;
import java.util.HashMap;
import java.util.Map;

public class DetectorColor {

    // Mapa de colores base para comparar
    private static final Map<String, Integer> COLORES_BASE = new HashMap<>();

    static {
        COLORES_BASE.put("Negro", Color.BLACK);
        COLORES_BASE.put("Blanco", Color.WHITE);
        COLORES_BASE.put("Gris", Color.LTGRAY);
        COLORES_BASE.put("Azul", Color.BLUE);
        COLORES_BASE.put("Rojo", Color.RED);
        COLORES_BASE.put("Verde", Color.GREEN);
        COLORES_BASE.put("Amarillo", Color.YELLOW);
        COLORES_BASE.put("Rosa", Color.MAGENTA);
        COLORES_BASE.put("Naranja", Color.parseColor("#FFA500"));
        COLORES_BASE.put("Morado", Color.parseColor("#800080"));
        COLORES_BASE.put("Beige", Color.parseColor("#F5F5DC"));
        COLORES_BASE.put("Marrón", Color.parseColor("#8B4513"));
    }

    public static String detectarColorDominante(Bitmap bitmap) {
        if (bitmap == null) return "Cualquiera";

        // 1. Extraer colores de la imagen
        Palette p = Palette.from(bitmap).generate();

        // Preferimos el color "Vibrante" o "Dominante"
        int colorDetectado = p.getDominantColor(Color.BLACK);
        if (p.getVibrantSwatch() != null) {
            colorDetectado = p.getVibrantSwatch().getRgb();
        } else if (p.getMutedSwatch() != null) {
            colorDetectado = p.getMutedSwatch().getRgb();
        }

        // 2. Buscar cual se parece más de nuestra lista
        return encontrarColorMasCercano(colorDetectado);
    }

    private static String encontrarColorMasCercano(int colorTarget) {
        double distanciaMinima = Double.MAX_VALUE;
        String nombreMasCercano = "Multicolor";

        int rT = Color.red(colorTarget);
        int gT = Color.green(colorTarget);
        int bT = Color.blue(colorTarget);

        for (Map.Entry<String, Integer> entry : COLORES_BASE.entrySet()) {
            int colorBase = entry.getValue();
            int rB = Color.red(colorBase);
            int gB = Color.green(colorBase);
            int bB = Color.blue(colorBase);

            // Fórmula de distancia Euclidiana (Pitágoras 3D)
            double distancia = Math.sqrt(Math.pow(rT - rB, 2) + Math.pow(gT - gB, 2) + Math.pow(bT - bB, 2));

            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                nombreMasCercano = entry.getKey();
            }
        }
        return nombreMasCercano;
    }
}