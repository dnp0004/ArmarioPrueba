package com.example.armariocamara;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClimaReal {

    // Clase estática para transportar los datos del clima
    public static class InfoClima {
        public double temperatura;
        public boolean esDia;
        public boolean lluvia; // <--- ESTE ES EL CAMPO QUE TE FALTABA
        public String descripcion;

        public InfoClima(double temperatura, boolean esDia, boolean lluvia, String descripcion) {
            this.temperatura = temperatura;
            this.esDia = esDia;
            this.lluvia = lluvia;
            this.descripcion = descripcion;
        }
    }

    // Método para obtener el clima real desde Internet (API Open-Meteo)
    public static InfoClima obtenerDatosCompletos(double lat, double lon) {
        try {
            // Conexión a API Gratuita (No requiere Clave API)
            String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current_weather=true";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 5 segundos de espera máximo

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            // Procesar la respuesta JSON
            JSONObject json = new JSONObject(result.toString());
            JSONObject current = json.getJSONObject("current_weather");

            double temp = current.getDouble("temperature");
            int isDay = current.getInt("is_day"); // 1 es día, 0 es noche
            int weatherCode = current.getInt("weathercode"); // Código numérico del clima

            // INTERPRETACIÓN DE CÓDIGOS DE LLUVIA (WMO Weather interpretation codes)
            // Códigos 51, 53, 55 son llovizna. 61, 63, 65 son lluvia. 80, 81, 82 son chubascos.
            // 71+ es nieve. 95+ es tormenta.
            boolean estaLloviendo = (weatherCode >= 50);

            String desc = "Normal";
            if (weatherCode <= 3) desc = "Despejado/Nubes";
            else if (weatherCode < 50) desc = "Niebla";
            else if (weatherCode < 70) desc = "Lluvia";
            else if (weatherCode < 80) desc = "Nieve";
            else desc = "Tormenta";

            return new InfoClima(temp, (isDay == 1), estaLloviendo, desc);

        } catch (Exception e) {
            e.printStackTrace();
            // Si falla la conexión o no hay internet, devolvemos un clima "Neutro" por defecto
            // para que la app no se cierre. (20 grados, sin lluvia)
            return new InfoClima(20.0, true, false, "Sin Conexión");
        }
    }
}