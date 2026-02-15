package com.example.armariocamara;

import java.util.*;

public class Estilista {

    // ══════════════════════════════════════════════════════════════
    //  MÉTODO AVANZADO (Llamado desde FormularioIAActivity)
    // ══════════════════════════════════════════════════════════════
    public List<Prenda> generarConjuntoProAvanzado(
            List<Prenda> armario,
            String ocasion,
            String colorFijo,
            List<Prenda> excluir,
            boolean esExterior,
            boolean haceFrio,
            boolean necesitaAccesorios,
            int numPiezasDeseadas,
            double temperatura,
            int duracionHoras,
            boolean posibleLluvia,
            List<String> coloresPreferidos,
            boolean muchoSol) {

        // Adaptamos los parámetros complejos a la lógica simple y añadimos filtros extra
        String colorPref = (coloresPreferidos != null && !coloresPreferidos.isEmpty()) ? coloresPreferidos.get(0) : colorFijo;
        boolean necesitaAbrigo = haceFrio || (temperatura < 18 && esExterior);

        // Llamamos a la lógica principal
        List<Prenda> outfitBase = generarConjuntoPro(
                armario, ocasion, colorPref, excluir, true, necesitaAbrigo, necesitaAccesorios
        );

        // Lógica Extra: Lluvia
        if (posibleLluvia && esExterior) {
            Prenda impermeable = buscarMejorDeCategoria(armario, "abrigo", ocasion, colorPref, null);
            // Intentar buscar algo impermeable específicamente si tienes metadatos, sino un abrigo sirve
            if (impermeable != null && !outfitBase.contains(impermeable)) {
                outfitBase.add(impermeable);
            }
        }

        // Lógica Extra: Mucho Sol (Gafas o Gorra)
        if (muchoSol && esExterior) {
            for (Prenda p : armario) {
                if (esCategoria(p, "accesorio")) {
                    String cat = p.categoria.toLowerCase();
                    if (cat.contains("gafas") || cat.contains("gorra") || cat.contains("sombrero")) {
                        if (!outfitBase.contains(p)) {
                            outfitBase.add(p);
                            break; // Solo uno
                        }
                    }
                }
            }
        }

        return outfitBase;
    }

    // ══════════════════════════════════════════════════════════════
    //  MÉTODO PRINCIPAL (Lógica Core)
    // ══════════════════════════════════════════════════════════════
    public List<Prenda> generarConjuntoPro(
            List<Prenda> armario, String ocasion, String colorPreferido,
            List<Prenda> excluir, boolean evitarRepetidos,
            boolean necesitaAbrigo, boolean necesitaAccesorios) {

        List<Prenda> resultado = new ArrayList<>();
        if (armario == null || armario.isEmpty()) return resultado;

        // Filtrar disponibles
        List<Prenda> disponibles = new ArrayList<>();
        for (Prenda p : armario) {
            if (!p.enLavanderia && (excluir == null || !excluir.contains(p))) {
                disponibles.add(p);
            }
        }
        if (disponibles.isEmpty()) return resultado;

        TipoOcasion tipo = clasificarOcasion(ocasion);

        switch (tipo) {
            case PLAYA: return generarOutfitPlaya(disponibles, ocasion, colorPreferido);
            case DEPORTE: return generarOutfitDeporte(disponibles, ocasion, colorPreferido);
            case FORMAL: return generarOutfitFormal(disponibles, ocasion, colorPreferido, necesitaAbrigo);
            default: return generarOutfitGeneral(disponibles, ocasion, colorPreferido, necesitaAbrigo, necesitaAccesorios);
        }
    }

    // [ ... AQUÍ PEGA EL RESTO DE MÉTODOS PRIVADOS (generarOutfitPlaya, etc.) ... ]
    // [ ... USAR EL CÓDIGO DE LA RESPUESTA ANTERIOR PARA EL RESTO DE LA CLASE ... ]

    // Te repito aquí los métodos privados esenciales para que no tengas errores de compilación:

    private List<Prenda> generarOutfitPlaya(List<Prenda> disponibles, String ocasion, String colorPref) {
        List<Prenda> resultado = new ArrayList<>();
        Prenda bano = buscarMejorDeCategoria(disponibles, "bano", ocasion, colorPref, null);
        if (bano != null) resultado.add(bano);

        Prenda zapatos = null;
        for (Prenda p : disponibles) {
            if (esCategoria(p, "zapatos") && resultado.stream().noneMatch(r -> r.id == p.id)) {
                String cat = p.categoria.toLowerCase();
                if (cat.contains("sandalia") || cat.contains("chancla") || cat.contains("flip")) {
                    if (zapatos == null || puntosEstilo(p, ocasion) > puntosEstilo(zapatos, ocasion)) zapatos = p;
                }
            }
        }
        if (zapatos != null) resultado.add(zapatos);
        return resultado;
    }

    private List<Prenda> generarOutfitDeporte(List<Prenda> disponibles, String ocasion, String colorPref) {
        List<Prenda> resultado = new ArrayList<>();
        Prenda superior = buscarMejorDeCategoria(disponibles, "deporte", ocasion, colorPref, null);
        if (superior == null) superior = buscarMejorDeCategoria(disponibles, "superior", ocasion, colorPref, null);
        if (superior != null) resultado.add(superior);

        Prenda inferior = buscarMejorDeCategoria(disponibles, "inferior", ocasion, colorPref, superior);
        if (inferior != null) resultado.add(inferior);

        Prenda zapatos = null;
        for (Prenda p : disponibles) {
            if (esCategoria(p, "zapatos")) {
                String cat = p.categoria.toLowerCase();
                if (cat.contains("deportiva") || cat.contains("sneaker")) {
                    if (zapatos == null) zapatos = p;
                }
            }
        }
        if (zapatos != null) resultado.add(zapatos);
        return resultado;
    }

    private List<Prenda> generarOutfitFormal(List<Prenda> disponibles, String ocasion, String colorPref, boolean forzarAbrigo) {
        List<Prenda> resultado = new ArrayList<>();
        Prenda cuerpoEntero = buscarMejorDeCategoria(disponibles, "cuerpo", ocasion, colorPref, null);
        if (cuerpoEntero != null) {
            resultado.add(cuerpoEntero);
        } else {
            Prenda superior = buscarMejorDeCategoria(disponibles, "superior", ocasion, colorPref, null);
            if (superior != null) resultado.add(superior);
            Prenda inferior = buscarMejorDeCategoria(disponibles, "inferior", ocasion, colorPref, superior);
            Prenda finalInferior = inferior;
            if (inferior != null && resultado.stream().noneMatch(r -> r.id == finalInferior.id)) resultado.add(inferior);
        }

        Prenda zapatos = buscarMejorDeCategoria(disponibles, "zapatos", ocasion, colorPref, resultado.isEmpty() ? null : resultado.get(0));
        Prenda finalZapatos = zapatos;
        if (zapatos != null && resultado.stream().noneMatch(r -> r.id == finalZapatos.id)) resultado.add(zapatos);

        if (forzarAbrigo) {
            Prenda abrigo = buscarMejorDeCategoria(disponibles, "abrigo", ocasion, colorPref, resultado.isEmpty() ? null : resultado.get(0));
            if (abrigo != null && !resultado.contains(abrigo)) resultado.add(abrigo);
        }
        return resultado;
    }

    private List<Prenda> generarOutfitGeneral(List<Prenda> disponibles, String ocasion, String colorPref, boolean necesitaAbrigo, boolean necesitaAccesorios) {
        List<Prenda> resultado = new ArrayList<>();

        Prenda cuerpoEntero = null;
        if (!ocasion.toLowerCase().contains("deporte")) {
            cuerpoEntero = buscarMejorDeCategoria(disponibles, "cuerpo", ocasion, colorPref, null);
        }

        Prenda superior = null;
        if (cuerpoEntero == null) {
            superior = buscarMejorDeCategoria(disponibles, "superior", ocasion, colorPref, null);
            if (superior != null) resultado.add(superior);
            Prenda inferior = buscarMejorDeCategoria(disponibles, "inferior", ocasion, colorPref, superior);
            if (inferior != null) resultado.add(inferior);
        } else {
            resultado.add(cuerpoEntero);
            superior = cuerpoEntero;
        }

        Prenda zapatos = buscarMejorDeCategoria(disponibles, "zapatos", ocasion, colorPref, superior);
        if (zapatos != null) resultado.add(zapatos);

        if (necesitaAbrigo) {
            Prenda abrigo = buscarMejorDeCategoria(disponibles, "abrigo", ocasion, colorPref, superior);
            if (abrigo != null) resultado.add(abrigo);
        }

        if (necesitaAccesorios) {
            Prenda accesorio = buscarMejorDeCategoria(disponibles, "accesorio", ocasion, colorPref, superior);
            if (accesorio != null) resultado.add(accesorio);
        }

        return resultado;
    }

    private Prenda buscarMejorDeCategoria(List<Prenda> disponibles, String tipoCat, String ocasion, String colorPref, Prenda referencia) {
        Prenda mejor = null;
        int mejorPuntos = -1;
        for (Prenda p : disponibles) {
            if (!esCategoria(p, tipoCat)) continue;
            int pts = puntuacionTotal(p, ocasion, colorPref, referencia);
            if (pts > mejorPuntos) {
                mejorPuntos = pts;
                mejor = p;
            }
        }
        return mejor;
    }

    private int puntuacionTotal(Prenda p, String ocasion, String colorPref, Prenda referencia) {
        int pts = 0;
        pts += puntosEstilo(p, ocasion);
        if (referencia != null && p.colores != null && referencia.colores != null && p.colores.equalsIgnoreCase(referencia.colores)) pts += 10;
        if (colorPref != null && p.colores != null && p.colores.toLowerCase().contains(colorPref.toLowerCase())) pts += 40;
        if (p.esFavorito) pts += 25;
        if (p.vecesUsada == 0) pts += 20;
        return pts;
    }

    private int puntosEstilo(Prenda p, String ocasion) {
        if (p.estilos == null || ocasion == null) return 0;
        if (p.estilos.toLowerCase().contains(ocasion.toLowerCase())) return 80;
        return 0;
    }

    public boolean esCategoria(Prenda p, String tipo) {
        if (p.categoria == null) return false;
        String cat = p.categoria.toLowerCase();
        switch (tipo) {
            case "superior": return cat.contains("superior") || cat.contains("camiseta") || cat.contains("camisa");
            case "inferior": return cat.contains("inferior") || cat.contains("pantalón") || cat.contains("falda");
            case "cuerpo":   return cat.contains("cuerpo") || cat.contains("vestido") || cat.contains("mono");
            case "abrigo":   return cat.contains("abrigo") || cat.contains("chaqueta");
            case "zapatos":  return cat.contains("zapato") || cat.contains("calzado") || cat.contains("botas");
            case "accesorio":return cat.contains("accesorio");
            case "bano":     return cat.contains("baño") || cat.contains("bikini");
            case "deporte":  return cat.contains("deportiva");
            default: return false;
        }
    }

    private TipoOcasion clasificarOcasion(String ocasion) {
        String oc = ocasion.toLowerCase();
        if (oc.contains("playa") || oc.contains("piscina")) return TipoOcasion.PLAYA;
        if (oc.contains("deporte") || oc.contains("gym")) return TipoOcasion.DEPORTE;
        if (oc.contains("formal") || oc.contains("gala") || oc.contains("boda")) return TipoOcasion.FORMAL;
        return TipoOcasion.GENERAL;
    }

    enum TipoOcasion { PLAYA, DEPORTE, FORMAL, GENERAL }
}