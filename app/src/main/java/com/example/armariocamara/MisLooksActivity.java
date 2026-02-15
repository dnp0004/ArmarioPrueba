package com.example.armariocamara;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MisLooksActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private FloatingActionButton btnCrearNuevo;
    private List<OutfitFavorito> listaFavoritos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_looks);

        recycler = findViewById(R.id.recyclerFavoritos);
        btnCrearNuevo = findViewById(R.id.btnCrearNuevo);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        if (btnCrearNuevo != null) {
            btnCrearNuevo.setOnClickListener(v -> {
                startActivity(new Intent(this, ArmarioActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarFavoritos();
    }

    private void cargarFavoritos() {
        new Thread(() -> {
            try {
                listaFavoritos = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerFavoritos();

                if (listaFavoritos == null) {
                    listaFavoritos = new ArrayList<>();
                }

                runOnUiThread(() -> {
                    if (listaFavoritos.isEmpty()) {
                        Toast.makeText(this, "No hay favoritos. Crea uno desde el armario.", Toast.LENGTH_LONG).show();
                    }

                    LooksAdapter adapter = new LooksAdapter(listaFavoritos, look -> {
                        if (look != null) mostrarOpciones(look);
                    });
                    recycler.setAdapter(adapter);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrarOpciones(OutfitFavorito look) {
        String[] opciones = {"👗 Ver / Editar", "📋 Duplicar", "✏️ Renombrar", "❌ Eliminar"};

        new AlertDialog.Builder(this)
                .setTitle(look.nombre != null ? look.nombre : "Outfit")
                .setItems(opciones, (d, which) -> {
                    switch (which) {
                        case 0: editarOutfitConPreviews(look); break;
                        case 1: duplicarOutfit(look); break;
                        case 2: renombrarOutfit(look); break;
                        case 3: borrarOutfit(look); break;
                    }
                })
                .show();
    }

    // ✅ EDITAR CON PREVIEWS
    private void editarOutfitConPreviews(OutfitFavorito look) {
        new Thread(() -> {
            List<Prenda> prendas = new ArrayList<>();
            int[] ids = {look.idTop, look.idBottom, look.idShoes, look.idOuter};

            for (int id : ids) {
                if (id != 0) {
                    Prenda p = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorId(id);
                    if (p != null) {
                        prendas.add(p);
                    }
                }
            }

            runOnUiThread(() -> {
                mostrarEditorOutfit(look, prendas);
            });
        }).start();
    }

    private void mostrarEditorOutfit(OutfitFavorito look, List<Prenda> prendas) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_editar_outfit_completo, null);

        TextView txtTitulo = view.findViewById(R.id.txtTituloEditorOutfit);
        LinearLayout containerPrendas = view.findViewById(R.id.containerPrendasEditor);
        Button btnGuardar = view.findViewById(R.id.btnGuardarEdicionOutfit);

        txtTitulo.setText(look.nombre);

        // Mostrar prendas editables
        for (int i = 0; i < prendas.size(); i++) {
            final int index = i;
            Prenda p = prendas.get(i);

            View itemView = LayoutInflater.from(this).inflate(R.layout.item_prenda_editable, containerPrendas, false);

            ImageView img = itemView.findViewById(R.id.imgPrendaEditable);
            TextView txtNombre = itemView.findViewById(R.id.txtNombrePrendaEditable);
            Button btnCambiar = itemView.findViewById(R.id.btnCambiarPrendaEditable);
            Button btnEliminar = itemView.findViewById(R.id.btnEliminarPrendaEditable);

            if (p.rutaImagen != null && new File(p.rutaImagen).exists()) {
                Glide.with(this).load(new File(p.rutaImagen)).centerCrop().into(img);
            }

            txtNombre.setText(p.subtipo);

            btnCambiar.setOnClickListener(v -> {
                abrirSelectorPrenda(p.categoria, prenda -> {
                    prendas.set(index, prenda);
                    mostrarEditorOutfit(look, prendas); // Refrescar
                });
            });

            btnEliminar.setOnClickListener(v -> {
                prendas.remove(index);
                mostrarEditorOutfit(look, prendas); // Refrescar
            });

            containerPrendas.addView(itemView);
        }

        AlertDialog dialog = builder.setView(view).create();

        btnGuardar.setOnClickListener(v -> {
            // Actualizar outfit
            if (prendas.size() > 0) look.idTop = prendas.get(0).id;
            if (prendas.size() > 1) look.idBottom = prendas.get(1).id;
            if (prendas.size() > 2) look.idShoes = prendas.get(2).id;
            if (prendas.size() > 3) look.idOuter = prendas.get(3).id;

            new Thread(() -> {
                COMPLETO_AppDatabase.getDb(this).prendaDao().actualizarFavorito(look);
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Outfit actualizado", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    cargarFavoritos();
                });
            }).start();
        });

        dialog.show();
    }

    // ✅ SELECTOR DE PRENDA con búsqueda y preview
    private void abrirSelectorPrenda(String categoria, OnPrendaSeleccionada callback) {
        new Thread(() -> {
            List<Prenda> opciones = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorCategoria(categoria);

            runOnUiThread(() -> {
                if (opciones == null || opciones.isEmpty()) {
                    Toast.makeText(this, "No hay prendas de esta categoría", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Crear diálogo con RecyclerView de prendas
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_selector_prenda, null);

                RecyclerView recyclerSelector = dialogView.findViewById(R.id.recyclerSelector);
                EditText etBuscar = dialogView.findViewById(R.id.etBuscarSelector);

                recyclerSelector.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

                List<Prenda> prendasFiltradas = new ArrayList<>(opciones);

                PrendaAdapter adapter = new PrendaAdapter(this, prendasFiltradas, prenda -> {
                    callback.onPrendaSeleccionada(prenda);
                    // Cerrar diálogo después de seleccionar
                });
                recyclerSelector.setAdapter(adapter);

                etBuscar.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        prendasFiltradas.clear();
                        String query = s.toString().toLowerCase();
                        for (Prenda p : opciones) {
                            String texto = (p.subtipo + " " + p.colores + " " + p.estilos).toLowerCase();
                            if (texto.contains(query)) {
                                prendasFiltradas.add(p);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });

                AlertDialog selectorDialog = builder.setView(dialogView).create();
                selectorDialog.show();
            });
        }).start();
    }

    interface OnPrendaSeleccionada {
        void onPrendaSeleccionada(Prenda prenda);
    }

    private void duplicarOutfit(OutfitFavorito original) {
        EditText input = new EditText(this);
        String nombreOriginal = original.nombre != null ? original.nombre : "Outfit";
        input.setText(nombreOriginal + " (Copia)");
        input.setTextColor(Color.BLACK);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Nombre de la copia")
                .setView(input)
                .setPositiveButton("Duplicar", (d, w) -> {
                    String nuevoNombre = input.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) nuevoNombre = nombreOriginal + " (Copia)";

                    OutfitFavorito copia = new OutfitFavorito(nuevoNombre, original.tipoGeneracion,
                            original.estilo, original.idTop, original.idBottom, original.idShoes,
                            original.idOuter, System.currentTimeMillis());

                    new Thread(() -> {
                        try {
                            COMPLETO_AppDatabase.getDb(this).prendaDao().guardarFavorito(copia);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Duplicado", Toast.LENGTH_SHORT).show();
                                cargarFavoritos();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void renombrarOutfit(OutfitFavorito look) {
        EditText input = new EditText(this);
        input.setText(look.nombre != null ? look.nombre : "");
        input.setTextColor(Color.BLACK);
        if (input.getText().length() > 0) input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Nuevo nombre")
                .setView(input)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nuevoNombre = input.getText().toString().trim();
                    if (!nuevoNombre.isEmpty()) {
                        look.nombre = nuevoNombre;

                        new Thread(() -> {
                            try {
                                COMPLETO_AppDatabase.getDb(this).prendaDao().actualizarFavorito(look);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                                    cargarFavoritos();
                                });
                            } catch (Exception e) {
                                runOnUiThread(() -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show());
                            }
                        }).start();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void borrarOutfit(OutfitFavorito look) {
        String nombreOutfit = look.nombre != null ? look.nombre : "este outfit";

        new AlertDialog.Builder(this)
                .setTitle("Borrar outfit")
                .setMessage("¿Seguro que quieres borrar '" + nombreOutfit + "'?")
                .setPositiveButton("SÍ", (d, w) -> {
                    new Thread(() -> {
                        try {
                            COMPLETO_AppDatabase.getDb(this).prendaDao().borrarFavorito(look);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Borrado", Toast.LENGTH_SHORT).show();
                                cargarFavoritos();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("NO", null)
                .show();
    }
}