package com.example.armariocamara;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity de estadísticas mejorado con visualizaciones
 */
public class EstadisticasActivity extends AppCompatActivity {

    private TextView txtTotalPrendas, txtPrendasLimpias, txtPrendasSucias;
    private TextView txtCategoriaTop, txtPrendaMasUsada, txtNuncaUsadas;
    private TextView txtPorCategoria, txtPorColor, txtPorMarca;
    private ProgressBar progressBar;
    private CardView cardStats1, cardStats2, cardStats3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        inicializarVistas();
        cargarEstadisticas();
    }

    private void inicializarVistas() {
        // Cards principales
        cardStats1 = findViewById(R.id.cardStats1);
        cardStats2 = findViewById(R.id.cardStats2);
        cardStats3 = findViewById(R.id.cardStats3);

        // Estadísticas generales
        txtTotalPrendas = findViewById(R.id.txtTotalPrendas);
        txtPrendasLimpias = findViewById(R.id.txtPrendasLimpias);
        txtPrendasSucias = findViewById(R.id.txtPrendasSucias);

        // Top stats
        txtCategoriaTop = findViewById(R.id.txtCategoriaTop);
        txtPrendaMasUsada = findViewById(R.id.txtPrendaMasUsada);
        txtNuncaUsadas = findViewById(R.id.txtNuncaUsadas);

        // Distribución
        txtPorCategoria = findViewById(R.id.txtPorCategoria);
        txtPorColor = findViewById(R.id.txtPorColor);
        txtPorMarca = findViewById(R.id.txtPorMarca);

        progressBar = findViewById(R.id.progressBarEstadisticas);
    }

    private void cargarEstadisticas() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                COMPLETO_PrendaDao dao = COMPLETO_AppDatabase.getDb(this).prendaDao();

                // Obtener todas las prendas
                List<Prenda> todasPrendas = dao.obtenerLimpias();
                if (todasPrendas == null) todasPrendas = new ArrayList<>();

                // Calcular estadísticas
                int total = todasPrendas.size();
                int limpias = 0;
                int sucias = 0;

                Map<String, Integer> porCategoria = new HashMap<>();
                Map<String, Integer> porColor = new HashMap<>();
                Map<String, Integer> porMarca = new HashMap<>();

                for (Prenda p : todasPrendas) {
                    // Contar limpias/sucias
                    if (p.enLavanderia) {
                        sucias++;
                    } else {
                        limpias++;
                    }

                    // Contar por categoría
                    porCategoria.put(p.categoria,
                            porCategoria.getOrDefault(p.categoria, 0) + 1);

                    // Contar por color principal
                    String colorPrincipal = p.colores != null && p.colores.contains(",")
                            ? p.colores.split(",")[0]
                            : p.colores;
                    porColor.put(colorPrincipal,
                            porColor.getOrDefault(colorPrincipal, 0) + 1);

                    // Contar por marca
                    porMarca.put(p.marca,
                            porMarca.getOrDefault(p.marca, 0) + 1);
                }

                // Encontrar categoría con más prendas
                String categoriaTop = encontrarMax(porCategoria);

                // Encontrar marca con más prendas
                String marcaTop = encontrarMax(porMarca);

                // Preparar strings de distribución
                String distCategoria = crearStringDistribucion(porCategoria);
                String distColor = crearStringDistribucion(porColor);
                String distMarca = crearStringDistribucion(porMarca);

                // Valores finales para UI
                final int totalFinal = total;
                final int limpiasFinal = limpias;
                final int suciasFinal = sucias;
                final String categoriaTopFinal = categoriaTop;
                final String marcaTopFinal = marcaTop;
                final String distCategoriaFinal = distCategoria;
                final String distColorFinal = distColor;
                final String distMarcaFinal = distMarca;

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    // Mostrar estadísticas generales
                    txtTotalPrendas.setText(String.valueOf(totalFinal));
                    txtPrendasLimpias.setText(limpiasFinal + " limpias (" +
                            (totalFinal > 0 ? (limpiasFinal * 100 / totalFinal) : 0) + "%)");
                    txtPrendasSucias.setText(suciasFinal + " en lavandería (" +
                            (totalFinal > 0 ? (suciasFinal * 100 / totalFinal) : 0) + "%)");

                    // Top stats
                    txtCategoriaTop.setText(categoriaTopFinal != null ? categoriaTopFinal : "N/A");
                    txtPrendaMasUsada.setText(marcaTopFinal != null ? marcaTopFinal : "N/A");
                    txtNuncaUsadas.setText("0"); // TODO: implementar contador de usos

                    // Distribución
                    txtPorCategoria.setText(distCategoriaFinal);
                    txtPorColor.setText(distColorFinal);
                    txtPorMarca.setText(distMarcaFinal);

                    // Animar cards
                    animarCards();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    txtTotalPrendas.setText("Error");
                });
            }
        }).start();
    }

    /**
     * Encontrar la clave con el valor máximo en un mapa
     */
    private String encontrarMax(Map<String, Integer> mapa) {
        String max = null;
        int valorMax = 0;

        for (Map.Entry<String, Integer> entry : mapa.entrySet()) {
            if (entry.getValue() > valorMax) {
                valorMax = entry.getValue();
                max = entry.getKey();
            }
        }

        return max;
    }

    /**
     * Crear string de distribución formateado
     */
    private String crearStringDistribucion(Map<String, Integer> mapa) {
        if (mapa.isEmpty()) return "Sin datos";

        StringBuilder sb = new StringBuilder();

        // Ordenar por valor (mayor a menor)
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(mapa.entrySet());
        lista.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Mostrar top 5
        int count = 0;
        for (Map.Entry<String, Integer> entry : lista) {
            if (count >= 5) break;

            sb.append("• ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
            count++;
        }

        return sb.toString().trim();
    }

    /**
     * Animar la aparición de las cards
     */
    private void animarCards() {
        cardStats1.setAlpha(0f);
        cardStats2.setAlpha(0f);
        cardStats3.setAlpha(0f);

        cardStats1.animate().alpha(1f).setDuration(500).setStartDelay(100).start();
        cardStats2.animate().alpha(1f).setDuration(500).setStartDelay(300).start();
        cardStats3.animate().alpha(1f).setDuration(500).setStartDelay(500).start();
    }
}