package com.example.armariocamara;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "outfits_planificados")  // CORREGIDO de "tabla_agenda"
public class OutfitPlanificado {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String fecha;              // "yyyy-MM-dd"
    public String titulo;             // "Reunión de trabajo", "Cena romántica"

    public int idTop;
    public int idBottom;
    public int idShoes;
    public int idOuter;

    public long fechaCreacion;
    public boolean puesto;            // Si ya se ha usado o no

    // Constructor vacío OBLIGATORIO
    public OutfitPlanificado() {
        this.fechaCreacion = System.currentTimeMillis();
        this.puesto = false;
    }

    // Constructor con parámetros
    @Ignore
    public OutfitPlanificado(String fecha, String titulo,
                             int idTop, int idBottom, int idShoes, int idOuter,
                             long fechaCreacion, boolean puesto) {
        this.fecha = fecha;
        this.titulo = titulo;
        this.idTop = idTop;
        this.idBottom = idBottom;
        this.idShoes = idShoes;
        this.idOuter = idOuter;
        this.fechaCreacion = fechaCreacion;
        this.puesto = puesto;
    }
}