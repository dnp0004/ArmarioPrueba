package com.example.armariocamara;

public class ParametrosIA {
    public String ocasion;
    public double temperatura;
    public boolean esExterior;
    public boolean hayLluvia;
    public boolean esFormal;
    public boolean esDeportivo;
    public int nivelCapas;
    public String duracion;
    public String actividad;

    public ParametrosIA(String ocasion, double temperatura, boolean esExterior,
                        boolean hayLluvia, boolean esFormal, boolean esDeportivo,
                        int nivelCapas, String duracion, String actividad) {
        this.ocasion = ocasion;
        this.temperatura = temperatura;
        this.esExterior = esExterior;
        this.hayLluvia = hayLluvia;
        this.esFormal = esFormal;
        this.esDeportivo = esDeportivo;
        this.nivelCapas = nivelCapas;
        this.duracion = duracion;
        this.actividad = actividad;
    }
}