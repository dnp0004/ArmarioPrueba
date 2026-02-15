package com.example.armariocamara;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArmarioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PrendaAdapter adapter;
    private TextView txtContador;
    private SearchView searchView;
    private ProgressBar progressBar;
    private FloatingActionButton fabMagic, fabCalendario, fabFiltros;
    private List<Prenda> todasLasPrendas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_armario);

        // 1. Inicializar Vistas
        txtContador = findViewById(R.id.txtContador);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerArmario);
        progressBar = findViewById(R.id.progressBarArmario);
        fabMagic = findViewById(R.id.btnMagic);
        fabCalendario = findViewById(R.id.btnAbrirCalendario);
        fabFiltros = findViewById(R.id.btnFiltros);

        // 2. Configurar RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // 3. Configurar Botones
        fabMagic.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioIAActivity.class);
            startActivity(intent);
        });

        fabCalendario.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarioActivity.class);
            startActivity(intent);
        });

        fabFiltros.setOnClickListener(v -> mostrarDialogoFiltros());

        // 4. Configurar Buscador
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filtrar(newText);
                }
                return true;
            }
        });

        // 5. Cargar datos iniciales
        cargarPrendas();
    }

    private void cargarPrendas() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // CORRECCIÓN: Usando el método correcto del DAO
                todasLasPrendas = COMPLETO_AppDatabase.getDb(this)
                        .prendaDao()
                        .obtenerTodasPrendas(); 

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    txtContador.setText(todasLasPrendas.size() + " prendas");

                    // Inicializar adaptador
                    adapter = new PrendaAdapter(this, todasLasPrendas, prenda -> {
                        // Al hacer clic, ir a editar
                        Intent intent = new Intent(ArmarioActivity.this, EditarPrendaActivity.class);
                        intent.putExtra("prenda_id", prenda.id);
                        startActivity(intent);
                    });

                    recyclerView.setAdapter(adapter);
                });

            } catch (Exception e) {
                Log.e("ArmarioActivity", "Error al cargar prendas: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar prendas", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrarDialogoFiltros() {
        FiltrosDialog dialog = new FiltrosDialog(this, filtros -> {
            aplicarFiltros(filtros);
        });
        dialog.show();
    }

    private void aplicarFiltros(FiltrosDialog.Filtros filtros) {
        new Thread(() -> {
            try {
                List<Prenda> prendasFiltradas = new ArrayList<>();

                for (Prenda p : todasLasPrendas) {
                    boolean cumpleFiltros = true;

                    // 1. Filtro de estilo
                    if (filtros.estilosSeleccionados != null && !filtros.estilosSeleccionados.isEmpty()) {
                        boolean tieneEstilo = false;
                        if (p.estilos != null && !p.estilos.isEmpty()) {
                            // p.estilos es un String, se debe dividir para comparar
                            String[] estilosArray = p.estilos.split(",");
                            for (String estilo : estilosArray) {
                                if (filtros.estilosSeleccionados.contains(estilo.trim())) {
                                    tieneEstilo = true;
                                    break;
                                }
                            }
                        }
                        if (!tieneEstilo) cumpleFiltros = false;
                    }

                    // 2. Filtro de color
                    if (cumpleFiltros && filtros.coloresSeleccionados != null && !filtros.coloresSeleccionados.isEmpty()) {
                        boolean tieneColor = false;
                        if (p.colores != null && !p.colores.isEmpty()) {
                            // p.colores es un String, se debe dividir
                            String[] coloresArray = p.colores.split(",");
                            for (String color : coloresArray) {
                                if (filtros.coloresSeleccionados.contains(color.trim())) {
                                    tieneColor = true;
                                    break;
                                }
                            }
                        }
                        if (!tieneColor) cumpleFiltros = false;
                    }

                    // 3. Filtro de limpio/sucio (usando enLavanderia)
                    if (cumpleFiltros) {
                        if (filtros.soloLimpias && p.enLavanderia) {
                            cumpleFiltros = false;
                        }
                        if (filtros.soloSucias && !p.enLavanderia) {
                            cumpleFiltros = false;
                        }
                    }

                    if (cumpleFiltros) {
                        prendasFiltradas.add(p);
                    }
                }

                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.actualizarLista(prendasFiltradas);
                    }
                    txtContador.setText(prendasFiltradas.size() + " prendas");
                    if (prendasFiltradas.isEmpty()) {
                        Toast.makeText(this, "No hay prendas que coincidan", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("ArmarioActivity", "Error aplicando filtros: " + e.getMessage());
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrendas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_armario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_filtros) {
            mostrarDialogoFiltros();
            return true;
        } else if (id == R.id.menu_personalizar) {
            Intent intent = new Intent(this, PersonalizadorOutfitActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
