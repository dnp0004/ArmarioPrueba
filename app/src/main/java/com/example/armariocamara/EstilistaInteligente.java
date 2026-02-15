package com.example.armariocamara;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EstilistaInteligente {

    private Random random = new Random();
    public String razonamiento = "";

    private class Candidato implements Comparable<Candidato> {
        Prenda prenda;
        int puntuacion;

        public Candidato(Prenda p, int pts) {
            this.prenda = p;
            this.puntuacion = pts;
        }

        @Override
        public int compareTo(Candidato o) {
            return o.puntuacion - this.puntuacion;
        }
    }

    public List<Prenda> generarOutfitInteligente(List<Prenda> armario, ParametrosIA params) {

        List<Prenda> outfit = new ArrayList<>();
        List<Prenda> disponibles = new ArrayList<>();

        // Filtrar prendas disponibles
        for (Prenda p : armario) {
            if (!p.enLavanderia && validarPrendaConParametros(p, params)) {
                disponibles.add(p);
            }
        }

        if (disponibles.isEmpty()) {
            razonamiento = "No hay ropa limpia compatible con estas condiciones.";
            return outfit;
        }

        // ✅ VALIDACIÓN INTELIGENTE: Detectar incompatibilidades
        if (params.ocasion.toLowerCase().contains("playa") || params.ocasion.toLowerCase().contains("piscina")) {
            return generarOutfitPlaya(disponibles, params);
        }

        if (params.ocasion.toLowerCase().contains("deporte") || params.ocasion.toLowerCase().contains("gym")) {
            return generarOutfitDeportivo(disponibles, params);
        }

        if (params.ocasion.toLowerCase().contains("formal") || params.ocasion.toLowerCase().contains("boda") || params.ocasion.toLowerCase().contains("gala")) {
            return generarOutfitFormal(disponibles, params);
        }

        // Outfit general
        return generarOutfitGeneral(disponibles, params);
    }

    // ✅ OUTFIT PLAYA: Solo traje de baño + sandalias + opcional pareo
    private List<Prenda> generarOutfitPlaya(List<Prenda> disponibles, ParametrosIA params) {
        List<Prenda> outfit = new ArrayList<>();

        Prenda bañador = buscarPorCategoria(disponibles, "baño");
        if (bañador != null) {
            outfit.add(bañador);
        }

        Prenda sandalias = buscarPorSubtipo(disponibles, "sandalia", "chancla", "flip");
        if (sandalias != null) {
            outfit.add(sandalias);
        }

        // Si hace frío, añadir pareo o camiseta ligera
        if (params.temperatura < 20) {
            Prenda capa = buscarPorCategoria(disponibles, "superior");
            if (capa != null && !outfit.contains(capa)) {
                outfit.add(capa);
            }
        }

        razonamiento = "Outfit de playa generado con " + outfit.size() + " piezas.";
        return outfit;
    }

    // ✅ OUTFIT DEPORTIVO: Ropa deportiva completa
    private List<Prenda> generarOutfitDeportivo(List<Prenda> disponibles, ParametrosIA params) {
        List<Prenda> outfit = new ArrayList<>();

        Prenda superior = buscarPorSubtipo(disponibles, "deportiva", "técnica", "running");
        if (superior != null) {
            outfit.add(superior);
        }

        Prenda inferior = buscarPorSubtipo(disponibles, "mallas", "short deportivo", "pantalón deportivo");
        if (inferior != null) {
            outfit.add(inferior);
        }

        Prenda calzado = buscarPorSubtipo(disponibles, "zapatillas deportivas", "running", "training");
        if (calzado != null) {
            outfit.add(calzado);
        }

        // Si hace frío, añadir sudadera
        if (params.temperatura < 15) {
            Prenda sudadera = buscarPorSubtipo(disponibles, "sudadera", "chaqueta deportiva");
            if (sudadera != null && !outfit.contains(sudadera)) {
                outfit.add(sudadera);
            }
        }

        razonamiento = "Outfit deportivo generado.";
        return outfit;
    }

    // ✅ OUTFIT FORMAL: Elegante y coherente
    private List<Prenda> generarOutfitFormal(List<Prenda> disponibles, ParametrosIA params) {
        List<Prenda> outfit = new ArrayList<>();

        // Buscar vestido primero
        Prenda vestido = buscarPorCategoria(disponibles, "vestido");
        if (vestido != null) {
            outfit.add(vestido);
        } else {
            // Si no hay vestido, camisa/blusa + pantalón formal
            Prenda camisa = buscarPorSubtipo(disponibles, "camisa", "blusa");
            if (camisa != null) {
                outfit.add(camisa);
            }

            Prenda pantalon = buscarPorSubtipo(disponibles, "pantalón", "falda");
            if (pantalon != null && !esCuerpoEntero(camisa)) {
                outfit.add(pantalon);
            }
        }

        // Zapatos formales
        Prenda zapatos = buscarPorSubtipo(disponibles, "tacones", "zapatos formales", "oxford");
        if (zapatos != null) {
            outfit.add(zapatos);
        }

        // Chaqueta si es necesario
        if (params.temperatura < 18 || params.esExterior) {
            Prenda americana = buscarPorSubtipo(disponibles, "americana", "blazer", "chaqueta");
            if (americana != null && !outfit.contains(americana)) {
                outfit.add(americana);
            }
        }

        razonamiento = "Outfit formal generado.";
        return outfit;
    }

    // ✅ OUTFIT GENERAL: Combinación inteligente
    private List<Prenda> generarOutfitGeneral(List<Prenda> disponibles, ParametrosIA params) {
        List<Prenda> outfit = new ArrayList<>();

        int numPrendas = calcularNumeroPrendas(params);

        // 1. PARTE SUPERIOR (obligatoria)
        Prenda superior = buscarMejor(disponibles, "superior", params, null);
        if (superior != null) {
            outfit.add(superior);
        }

        // 2. PARTE INFERIOR (solo si superior no es vestido)
        if (superior != null && !esCuerpoEntero(superior)) {
            Prenda inferior = buscarMejor(disponibles, "inferior", params, superior);
            if (inferior != null) {
                outfit.add(inferior);
            }
        }

        // 3. CALZADO (obligatorio)
        Prenda referencia = outfit.size() > 1 ? outfit.get(1) : superior;
        Prenda calzado = buscarMejor(disponibles, "calzado", params, referencia);
        if (calzado != null) {
            outfit.add(calzado);
        }

        // ✅ VALIDACIÓN: No sandalias si hace frío
        if (calzado != null && params.temperatura < 15) {
            String subtipo = calzado.subtipo != null ? calzado.subtipo.toLowerCase() : "";
            if (subtipo.contains("sandalia") || subtipo.contains("chancla")) {
                // Buscar alternativa
                Prenda alternativa = buscarCalzadoCerrado(disponibles, params);
                if (alternativa != null) {
                    outfit.remove(calzado);
                    outfit.add(alternativa);
                }
            }
        }

        // 4. CAPAS ADICIONALES según nivelCapas
        if (params.nivelCapas >= 1 && outfit.size() < numPrendas) {
            // Jersey o sudadera
            if (params.temperatura < 20) {
                Prenda jersey = buscarPorCategoria(disponibles, "jersey");
                if (jersey != null && !outfit.contains(jersey)) {
                    outfit.add(jersey);
                }
            }
        }

        if (params.nivelCapas >= 2 && outfit.size() < numPrendas) {
            // Abrigo o chaqueta
            if (params.temperatura < 15 || params.esExterior) {
                Prenda abrigo = buscarPorCategoria(disponibles, "abrigo");
                if (abrigo != null && !outfit.contains(abrigo)) {
                    outfit.add(abrigo);
                }
            }
        }

        // 5. LLUVIA: Añadir impermeable
        if (params.hayLluvia && params.esExterior) {
            Prenda impermeable = buscarPorSubtipo(disponibles, "impermeable", "chubasquero");
            if (impermeable != null && !outfit.contains(impermeable)) {
                outfit.add(impermeable);
            }
        }

        // 6. ACCESORIOS si hay espacio
        if (params.nivelCapas >= 3 && outfit.size() < numPrendas) {
            Prenda accesorio = buscarPorCategoria(disponibles, "accesorio");
            if (accesorio != null && !outfit.contains(accesorio)) {
                outfit.add(accesorio);
            }
        }

        razonamiento = "Outfit generado con " + outfit.size() + " prendas para " + params.ocasion;
        return outfit;
    }

    private int calcularNumeroPrendas(ParametrosIA params) {
        switch (params.nivelCapas) {
            case 0: return 3; // Mínimo
            case 1: return 4; // Normal
            case 2: return 5; // Capas
            case 3: return 6; // Muchas capas
            default: return 4;
        }
    }

    private boolean validarPrendaConParametros(Prenda p, ParametrosIA params) {
        String cat = p.categoria != null ? p.categoria.toLowerCase() : "";
        String sub = p.subtipo != null ? p.subtipo.toLowerCase() : "";

        // No ropa de baño si hace frío
        if (cat.contains("baño") && params.temperatura < 15) {
            return false;
        }

        // No sandalias si hace frío
        if ((sub.contains("sandalia") || sub.contains("chancla")) && params.temperatura < 10) {
            return false;
        }

        // No ropa deportiva si es formal
        if (params.esFormal && cat.contains("deporte")) {
            return false;
        }

        // No vestidos si es deporte
        if (params.esDeportivo && cat.contains("vestido")) {
            return false;
        }

        return true;
    }

    private Prenda buscarMejor(List<Prenda> pool, String categoria, ParametrosIA params, Prenda referencia) {
        List<Candidato> ranking = new ArrayList<>();

        for (Prenda p : pool) {
            if (!perteneceACategoria(p, categoria)) continue;

            int puntos = 0;

            // Puntos por ocasión
            String estilos = p.estilos != null ? p.estilos.toLowerCase() : "";
            if (estilos.contains(params.ocasion.toLowerCase())) {
                puntos += 150;
            }

            // Puntos por temperatura
            if (params.temperatura < 15 && p.subtipo != null && p.subtipo.toLowerCase().contains("pesado")) {
                puntos += 100;
            }

            if (params.temperatura > 25 && p.subtipo != null && p.subtipo.toLowerCase().contains("ligero")) {
                puntos += 100;
            }

            // Puntos por combinación de colores
            if (referencia != null) {
                // Aquí usamos ColorUtils, asegúrate de tener esa clase o elimina esta parte si no la usas
                // String colorP = p.colores != null && !p.colores.isEmpty() ? p.colores.split(",")[0].trim() : "";
                // String colorRef = referencia.colores != null && !referencia.colores.isEmpty() ? referencia.colores.split(",")[0].trim() : "";
                // puntos += ColorUtils.calcularPuntuacionColor(colorP, colorRef);
                puntos += 10; // Placeholder si no tienes ColorUtils
            }

            // Favoritos
            if (p.esFavorito) {
                puntos += 30;
            }

            // Poco usadas - CORREGIDO AQUÍ
            if (p.vecesUsada < 3) {
                puntos += 20;
            }

            puntos += random.nextInt(30);

            ranking.add(new Candidato(p, puntos));
        }

        if (ranking.isEmpty()) return null;

        Collections.sort(ranking);
        return ranking.get(0).prenda;
    }

    private Prenda buscarPorCategoria(List<Prenda> pool, String keyword) {
        for (Prenda p : pool) {
            String cat = p.categoria != null ? p.categoria.toLowerCase() : "";
            if (cat.contains(keyword)) {
                return p;
            }
        }
        return null;
    }

    private Prenda buscarPorSubtipo(List<Prenda> pool, String... keywords) {
        for (Prenda p : pool) {
            String sub = p.subtipo != null ? p.subtipo.toLowerCase() : "";
            for (String keyword : keywords) {
                if (sub.contains(keyword.toLowerCase())) {
                    return p;
                }
            }
        }
        return null;
    }

    private Prenda buscarCalzadoCerrado(List<Prenda> pool, ParametrosIA params) {
        for (Prenda p : pool) {
            String cat = p.categoria != null ? p.categoria.toLowerCase() : "";
            String sub = p.subtipo != null ? p.subtipo.toLowerCase() : "";

            if (cat.contains("calzado") || cat.contains("zapato")) {
                if (!sub.contains("sandalia") && !sub.contains("chancla")) {
                    return p;
                }
            }
        }
        return null;
    }

    private boolean perteneceACategoria(Prenda p, String categoria) {
        String cat = p.categoria != null ? p.categoria.toLowerCase() : "";

        switch (categoria) {
            case "superior":
                return cat.contains("superior") || cat.contains("camiseta") || cat.contains("camisa") || cat.contains("blusa");
            case "inferior":
                return cat.contains("inferior") || cat.contains("pantalón") || cat.contains("falda");
            case "calzado":
                return cat.contains("calzado") || cat.contains("zapato");
            case "abrigo":
                return cat.contains("abrigo") || cat.contains("chaqueta");
            case "jersey":
                return cat.contains("jersey") || cat.contains("sudadera");
            case "accesorio":
                return cat.contains("accesorio");
            default:
                return false;
        }
    }

    private boolean esCuerpoEntero(Prenda p) {
        if (p == null) return false;
        String cat = p.categoria != null ? p.categoria.toLowerCase() : "";
        return cat.contains("vestido") || cat.contains("cuerpo") || cat.contains("mono");
    }
}