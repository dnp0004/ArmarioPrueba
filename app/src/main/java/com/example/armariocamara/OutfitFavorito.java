package com.example.armariocamara;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "outfits_favoritos")  // CORREGIDO de "tabla_favoritos"
public class OutfitFavorito {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;
    public String tipoGeneracion;    // "Manual", "IA", "Sugerencia"
    public String estilo;

    public int idTop;
    public int idBottom;
    public int idShoes;
    public int idOuter;

    public long fechaCreacion;

    // Constructor vacío OBLIGATORIO
    public OutfitFavorito() {
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Constructor con parámetros
    @Ignore
    public OutfitFavorito(String nombre, String tipoGeneracion, String estilo,
                          int idTop, int idBottom, int idShoes, int idOuter, long fechaCreacion) {
        this.nombre = nombre;
        this.tipoGeneracion = tipoGeneracion;
        this.estilo = estilo;
        this.idTop = idTop;
        this.idBottom = idBottom;
        this.idShoes = idShoes;
        this.idOuter = idOuter;
        this.fechaCreacion = fechaCreacion;
    }
}