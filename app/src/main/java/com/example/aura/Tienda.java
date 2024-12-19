package com.example.aura;

public class Tienda {
    private String nombre;
    private String fotoUrl;
    private String urlCompra;

    public Tienda() { }  // Constructor vacío necesario para Firestore

    public Tienda(String nombre, String fotoUrl, String urlCompra) {
        this.nombre = nombre;
        this.fotoUrl = fotoUrl;
        this.urlCompra = urlCompra;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getUrlCompra() {
        return urlCompra;
    }
}
