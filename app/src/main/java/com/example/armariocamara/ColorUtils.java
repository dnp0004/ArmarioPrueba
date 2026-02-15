package com.example.armariocamara;

public class ColorUtils {

    // Devuelve puntos de compatibilidad entre dos colores (0 a 100)
    public static int calcularPuntuacionColor(String c1, String c2) {
        if(c1 == null || c2 == null) return 0;
        c1 = c1.toLowerCase(); c2 = c2.toLowerCase();

        if(c1.equals(c2)) return 40; // Monocromático está bien
        if(c1.equals("negro") || c2.equals("negro")) return 100; // Negro pega con todo
        if(c1.equals("blanco") || c2.equals("blanco")) return 90; // Blanco pega con todo
        if(c1.equals("vaquero") || c2.equals("vaquero")) return 85;

        // Combinaciones clásicas
        if((c1.equals("azul") && c2.equals("beige")) || (c2.equals("azul") && c1.equals("beige"))) return 80;
        if((c1.equals("gris") && c2.equals("rosa")) || (c2.equals("gris") && c1.equals("rosa"))) return 70;

        return 30; // Por defecto
    }
}