package com.example.armariocamara;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "viajes")
public class Viaje {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nombre;
    public String destino;
    public int numDias;
    public String outfitsIds;  // IDs de los outfits separados por comas: "1,2,3,4"
    public long fechaCreacion;

    // Constructor vacío para Room
    public Viaje() {
        this.fechaCreacion = System.currentTimeMillis();
    }

    public Viaje(String nombre, String destino, int numDias, String outfitsIds, long fechaCreacion) {
        this.nombre = nombre;
        this.destino = destino;
        this.numDias = numDias;
        this.outfitsIds = outfitsIds;
        this.fechaCreacion = fechaCreacion;
    }
}