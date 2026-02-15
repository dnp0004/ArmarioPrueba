package com.example.armariocamara;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tabla_moodboard")
public class Moodboard {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;      // Ej: "Cena Elegante"
    public String rutaImagen;  // La foto de Pinterest
    public String coloresHex;  // Los colores extraídos (guardados como texto "int,int,int")

    public Moodboard(String nombre, String rutaImagen, String coloresHex) {
        this.nombre = nombre;
        this.rutaImagen = rutaImagen;
        this.coloresHex = coloresHex;
    }
}