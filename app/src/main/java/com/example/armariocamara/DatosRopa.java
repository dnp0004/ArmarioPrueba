package com.example.armariocamara;

import java.util.ArrayList;
import java.util.List;

public class DatosRopa {

    // ✅ ESTO FALTABA Y DABA EL ERROR
    public static final String[] OCASIONES = {
            "Casual / Diario",
            "Trabajo / Oficina",
            "Deporte / Gym",
            "Formal / Evento",
            "Fiesta / Noche",
            "Playa / Piscina",
            "Cita / Romántico",
            "Lluvia / Mal tiempo",
            "Estar en casa"
    };

    // ESTILOS (Dejamos los que tenías)
    public static final String[] ESTILOS = {
            "Casual Diario", "Smart Casual", "Comfy / Casa", "Trabajo / Oficina",
            "Fiesta Nocturna", "Cóctel", "Gala / Evento Formal", "Boda / Ceremonia",
            "Graduación", "Cena Romántica", "Primera Cita",
            "Verano / Playa", "Invierno / Nieve", "Otoño / Entretiempo", "Primavera",
            "Lluvia / Impermeable",
            "Deporte / Gym", "Running / Atletismo", "Yoga / Pilates", "Ciclismo",
            "Senderismo / Montaña", "Natación / Piscina",
            "Boho Chic", "Vintage / Retro", "Minimalista", "Streetwear / Urbano",
            "Rock / Grunge", "Preppy / Clásico", "Romántico / Femenino",
            "Gótico / Dark", "Hip Hop", "Elegante / Sofisticado",
            "Business Travel", "Vacaciones / Resort", "City Break", "Aventura",
            "Festival / Concierto", "Discoteca / Club", "Brunch / Café",
            "Shopping", "Aeropuerto", "Videollamada", "Casual Viernes"
    };

    // CATEGORÍAS DETALLADAS
    public static final String[] CATEGORIAS = {
            "Parte Superior - Camiseta Básica", "Parte Superior - Camiseta Estampada",
            "Parte Superior - Camiseta Tirantes / Tank Top", "Parte Superior - Polo",
            "Parte Superior - Camisa Manga Larga", "Parte Superior - Camisa Manga Corta",
            "Parte Superior - Blusa", "Parte Superior - Top Crop",
            "Parte Superior - Top Elegante", "Parte Superior - Body",
            "Parte Superior - Corsé / Bustier", "Parte Superior - Jersey Fino",
            "Parte Superior - Jersey Grueso", "Parte Superior - Sudadera / Hoodie",
            "Parte Superior - Sudadera sin Capucha", "Parte Superior - Cárdigan",
            "Parte Superior - Chaleco",
            "Parte Inferior - Pantalón Largo Formal", "Parte Inferior - Pantalón Casual",
            "Parte Inferior - Pantalón Corto / Bermudas", "Parte Inferior - Shorts Cortos",
            "Parte Inferior - Jeans / Vaqueros", "Parte Inferior - Jeans Rotos",
            "Parte Inferior - Pantalón Deportivo", "Parte Inferior - Leggings / Mallas",
            "Parte Inferior - Falda Mini", "Parte Inferior - Falda Midi",
            "Parte Inferior - Falda Larga / Maxi", "Parte Inferior - Falda Tubo",
            "Parte Inferior - Falda Plisada",
            "Cuerpo Entero - Vestido Corto", "Cuerpo Entero - Vestido Midi",
            "Cuerpo Entero - Vestido Largo / Maxi", "Cuerpo Entero - Vestido Cóctel",
            "Cuerpo Entero - Vestido Noche / Gala", "Cuerpo Entero - Mono Corto",
            "Cuerpo Entero - Mono Largo", "Cuerpo Entero - Peto / Overol",
            "Cuerpo Entero - Traje Chaqueta", "Cuerpo Entero - Traje Pantalón",
            "Abrigo - Chaqueta Vaquera", "Abrigo - Chaqueta Cuero / Piel",
            "Abrigo - Chaqueta Bomber", "Abrigo - Chaqueta Ligera",
            "Abrigo - Abrigo Entretiempo", "Abrigo - Abrigo Pesado / Invierno",
            "Abrigo - Plumífero / Puffer", "Abrigo - Parka",
            "Abrigo - Gabardina / Trench", "Abrigo - Blazer / Americana",
            "Abrigo - Impermeable / Chubasquero", "Abrigo - Abrigo Largo Elegante",
            "Zapatos - Deportivas / Sneakers", "Zapatos - Deportivas Running",
            "Zapatos - Botas Altas", "Zapatos - Botines / Botas Cortas",
            "Zapatos - Botas Militares", "Zapatos - Botas Cowboy",
            "Zapatos - Tacones Altos", "Zapatos - Tacones Medios",
            "Zapatos - Sandalias Planas", "Zapatos - Sandalias Tacón",
            "Zapatos - Chanclas / Flip-Flops", "Zapatos - Mocasines",
            "Zapatos - Bailarinas / Flats", "Zapatos - Zapatos Formales",
            "Zapatos - Alpargatas", "Zapatos - Zuecos / Mules",
            "Accesorio - Bolso Grande / Tote", "Accesorio - Bolso Pequeño / Clutch",
            "Accesorio - Bolso Bandolera", "Accesorio - Mochila",
            "Accesorio - Riñonera / Belt Bag", "Accesorio - Sombrero",
            "Accesorio - Gorra / Cap", "Accesorio - Gorro Invierno",
            "Accesorio - Boina", "Accesorio - Bufanda",
            "Accesorio - Pañuelo / Foulard", "Accesorio - Gafas de Sol",
            "Accesorio - Gafas Graduadas", "Accesorio - Cinturón",
            "Accesorio - Joyería / Bisutería", "Accesorio - Reloj",
            "Accesorio - Guantes",
            "Ropa de Baño - Bikini", "Ropa de Baño - Bañador Entero",
            "Ropa de Baño - Shorts de Baño", "Ropa Deportiva - Top Deportivo",
            "Ropa Deportiva - Camiseta Técnica", "Ropa Interior - Sujetador",
            "Ropa Interior - Ropa Interior Básica"
    };

    // COLORES
    public static final String[] COLORES = {
            "Negro", "Blanco", "Gris Claro", "Gris Medio", "Gris Oscuro",
            "Beige", "Crema", "Camel", "Marrón Claro", "Marrón Chocolate",
            "Marrón Oscuro", "Topo / Taupe", "Azul Marino", "Azul Oscuro",
            "Azul Real", "Azul Medio", "Azul Claro", "Azul Cielo", "Azul Bebé",
            "Turquesa", "Aguamarina", "Azul Eléctrico", "Azul Petróleo",
            "Rojo Vivo", "Rojo Oscuro", "Burdeos", "Vino", "Granate",
            "Rosa Palo", "Rosa Chicle", "Rosa Fucsia", "Rosa Viejo",
            "Coral", "Salmón", "Verde Militar", "Verde Oliva", "Verde Botella",
            "Verde Oscuro", "Verde Medio", "Verde Lima", "Verde Menta",
            "Verde Esmeralda", "Verde Agua", "Amarillo", "Amarillo Mostaza",
            "Amarillo Pastel", "Naranja", "Naranja Quemado", "Terracota",
            "Calabaza", "Morado", "Morado Oscuro", "Lila", "Lavanda",
            "Ciruela", "Berenjena", "Dorado", "Plateado", "Oro Rosa",
            "Bronce", "Cobre", "Multicolor", "Tie-Dye", "Degradado",
            "Estampado Animal (Leopardo)", "Estampado Animal (Cebra)",
            "Estampado Animal (Serpiente)", "Estampado Floral",
            "Estampado Geométrico", "Cuadros Escoceses", "Cuadros Vichy",
            "Rayas Horizontales", "Rayas Verticales", "Lunares / Topos",
            "Camuflaje"
    };

    // Helpers
    public static List<String> obtenerTallasPorCategoria(String categoria) {
        List<String> tallas = new ArrayList<>();
        if (categoria.contains("Zapatos") || categoria.contains("Calzado")) {
            for (int i = 35; i <= 47; i++) tallas.add(String.valueOf(i));
        } else if (categoria.contains("Pantalón") || categoria.contains("Jeans") || categoria.contains("Falda")) {
            tallas.add("XXS"); tallas.add("XS"); tallas.add("S"); tallas.add("M");
            tallas.add("L"); tallas.add("XL"); tallas.add("XXL");
            tallas.add("--- Numéricas ---");
            for (int i = 32; i <= 54; i += 2) tallas.add(String.valueOf(i));
        } else {
            tallas.add("XXS"); tallas.add("XS"); tallas.add("S"); tallas.add("M");
            tallas.add("L"); tallas.add("XL"); tallas.add("XXL");
        }
        return tallas;
    }

    public static String obtenerCategoriaPrincipal(String categoriaCompleta) {
        if (categoriaCompleta.contains("Parte Superior")) return "superior";
        if (categoriaCompleta.contains("Parte Inferior")) return "inferior";
        if (categoriaCompleta.contains("Cuerpo Entero")) return "cuerpo_entero";
        if (categoriaCompleta.contains("Abrigo")) return "abrigo";
        if (categoriaCompleta.contains("Zapatos")) return "zapatos";
        if (categoriaCompleta.contains("Accesorio")) return "accesorio";
        if (categoriaCompleta.contains("Ropa de Baño")) return "bano";
        if (categoriaCompleta.contains("Ropa Deportiva")) return "deporte";
        return "otro";
    }
}