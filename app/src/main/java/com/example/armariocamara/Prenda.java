package com.example.armariocamara;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "prendas")
public class Prenda {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String rutaImagen;
    public String nombreArchivo;
    public String categoria;
    public String subtipo;
    public String talla;

    // Campos múltiples
    public String estilos = "";
    public String colores = "";
    public String marca = "";

    // Estadísticas
    public int vecesUsada = 0;
    public long ultimoUso = 0;
    public boolean enLavanderia = false;
    public boolean esFavorito = false;

    // Metadatos
    public String notas = "";
    public String temporada = "";
    public long fechaCreacion = System.currentTimeMillis();

    // Constructor vacío OBLIGATORIO para Room
    public Prenda() {}

    // Constructor con parámetros (7 ARGUMENTOS)
    @Ignore
    public Prenda(String rutaImagen, String categoria, String subtipo, String talla,
                  String colores, String marca, String estilos) {
        this.rutaImagen = rutaImagen;
        this.categoria = categoria;
        this.subtipo = subtipo;
        this.talla = talla;
        this.colores = colores;
        this.marca = marca;
        this.estilos = estilos;
        this.fechaCreacion = System.currentTimeMillis();
    }
}