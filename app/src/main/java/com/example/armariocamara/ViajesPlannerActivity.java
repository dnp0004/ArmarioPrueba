package com.example.armariocamara;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViajesPlannerActivity extends AppCompatActivity {

    private EditText etNombre, etDestino;
    private SeekBar seekDias;
    private TextView txtDiasSeleccionados;
    private Button btnGenerar, btnGuardar; // Eliminado btnVerViajes porque no está en el XML
    private RecyclerView recyclerOutfits;
    private List<OutfitFavorito> outfitsDelViaje = new ArrayList<>();
    private OutfitsViajeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Asegúrate de que el XML se llame exactamente así
            setContentView(R.layout.activity_viajes_planner);

            // --- CORRECCIÓN DE IDs AQUÍ (Sincronizados con tu XML) ---
            etNombre = findViewById(R.id.inputNombreViaje);           // Antes: etNombreViaje
            etDestino = findViewById(R.id.inputDestino);              // Antes: etDestinoViaje
            seekDias = findViewById(R.id.seekNumOutfits);             // Antes: seekDias
            txtDiasSeleccionados = findViewById(R.id.txtNumOutfitsSeleccionados); // Antes: txtDiasSeleccionados
            btnGenerar = findViewById(R.id.btnGenerarOutfitsViaje);   // Antes: btnGenerarMaleta
            btnGuardar = findViewById(R.id.btnGuardarTodoViaje);      // Antes: btnGuardarViaje

            // Este botón NO existe en tu XML, así que lo he comentado para evitar crasheos:
            // btnVerViajes = findViewById(R.id.btnVerViajesGuardados);

            recyclerOutfits = findViewById(R.id.recyclerOutfitsViaje);

            recyclerOutfits.setLayoutManager(new LinearLayoutManager(this));
            adapter = new OutfitsViajeAdapter();
            recyclerOutfits.setAdapter(adapter);

            seekDias.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Mínimo 1 outfit
                    int cantidad = Math.max(1, progress);
                    txtDiasSeleccionados.setText(cantidad + " outfits");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            btnGenerar.setOnClickListener(v -> generarMaleta());
            btnGuardar.setOnClickListener(v -> guardarViaje());

            // btnVerViajes.setOnClickListener(v -> verViajesGuardados()); // Comentado

        } catch (Exception e) {
            Log.e("ViajesPlanner", "Error en onCreate: " + e.getMessage());
            Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void generarMaleta() {
        int numOutfits = Math.max(1, seekDias.getProgress());

        new Thread(() -> {
            try {
                List<Prenda> armario = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerLimpias();

                if (armario == null || armario.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "No tienes prendas en el armario", Toast.LENGTH_SHORT).show());
                    return;
                }

                outfitsDelViaje.clear();
                Estilista estilista = new Estilista();

                for (int i = 1; i <= numOutfits; i++) {
                    // Alternamos ocasión simple para el ejemplo
                    String ocasion = (i % 2 == 0) ? "Casual Diario" : "Salir de Noche";

                    // Generar conjunto
                    List<Prenda> outfit = estilista.generarConjuntoPro(armario, ocasion, null, new ArrayList<>(), false, false, false);

                    if (outfit != null && !outfit.isEmpty()) {
                        int top = 0, bottom = 0, shoes = 0, outer = 0;
                        if (outfit.size() > 0) top = outfit.get(0).id;
                        if (outfit.size() > 1) bottom = outfit.get(1).id;
                        if (outfit.size() > 2) shoes = outfit.get(2).id;
                        if (outfit.size() > 3) outer = outfit.get(3).id;

                        OutfitFavorito outfitViaje = new OutfitFavorito(
                                "Outfit " + i,
                                "Viaje",
                                ocasion,
                                top, bottom, shoes, outer,
                                System.currentTimeMillis()
                        );
                        outfitViaje.id = i; // ID temporal para visualización
                        outfitsDelViaje.add(outfitViaje);
                    }
                }

                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "✅ " + outfitsDelViaje.size() + " outfits generados", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e("ViajesPlanner", "Error generando maleta: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error al generar maleta", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void guardarViaje() {
        String nombre = etNombre.getText().toString().trim();
        String destino = etDestino.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe el nombre del viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        if (outfitsDelViaje.isEmpty()) {
            Toast.makeText(this, "Genera primero los outfits", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // Guardar cada outfit como favorito y obtener sus IDs reales
                List<Integer> idsReales = new ArrayList<>();

                for (OutfitFavorito outfit : outfitsDelViaje) {
                    long idReal = COMPLETO_AppDatabase.getDb(this).prendaDao().guardarFavorito(outfit);
                    idsReales.add((int) idReal);
                }

                // Crear string con IDs separados por comas
                StringBuilder outfitsIdsStr = new StringBuilder();
                for (int i = 0; i < idsReales.size(); i++) {
                    outfitsIdsStr.append(idsReales.get(i));
                    if (i < idsReales.size() - 1) {
                        outfitsIdsStr.append(",");
                    }
                }

                // Guardar el viaje
                Viaje viaje = new Viaje(
                        nombre,
                        destino,
                        outfitsDelViaje.size(),
                        outfitsIdsStr.toString(),
                        System.currentTimeMillis()
                );

                COMPLETO_AppDatabase.getDb(this).prendaDao().guardarViaje(viaje);

                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Viaje guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                Log.e("ViajesPlanner", "Error guardando viaje: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error al guardar viaje", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void verViajesGuardados() {
        new Thread(() -> {
            try {
                List<Viaje> viajes = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerViajes();

                runOnUiThread(() -> {
                    if (viajes == null || viajes.isEmpty()) {
                        Toast.makeText(this, "No tienes viajes guardados", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mostrarListaViajes(viajes);
                });

            } catch (Exception e) {
                Log.e("ViajesPlanner", "Error cargando viajes: " + e.getMessage());
            }
        }).start();
    }

    private void mostrarListaViajes(List<Viaje> viajes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lista_viajes, null);

        RecyclerView recyclerViajes = dialogView.findViewById(R.id.recyclerViajes);
        recyclerViajes.setLayoutManager(new LinearLayoutManager(this));

        ViajesAdapter viajesAdapter = new ViajesAdapter(viajes, viaje -> mostrarOpcionesViaje(viaje));
        recyclerViajes.setAdapter(viajesAdapter);

        builder.setView(dialogView)
                .setTitle("📋 Viajes Guardados")
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void mostrarOpcionesViaje(Viaje viaje) {
        String[] opciones = {"👁️ Ver Outfits", "❌ Eliminar"};

        new AlertDialog.Builder(this)
                .setTitle(viaje.nombre)
                .setItems(opciones, (d, which) -> {
                    switch (which) {
                        case 0: verOutfitsDelViaje(viaje); break;
                        case 1: eliminarViaje(viaje); break;
                    }
                })
                .show();
    }

    private void verOutfitsDelViaje(Viaje viaje) {
        new Thread(() -> {
            try {
                String[] idsStr = viaje.outfitsIds.split(",");
                List<OutfitFavorito> outfits = new ArrayList<>();

                for (String idStr : idsStr) {
                    if (!idStr.trim().isEmpty()) {
                        int id = Integer.parseInt(idStr.trim());
                        OutfitFavorito outfit = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerFavoritoPorId(id);
                        if (outfit != null) {
                            outfits.add(outfit);
                        }
                    }
                }

                runOnUiThread(() -> mostrarDialogoOutfits(outfits, viaje.nombre));

            } catch (Exception e) {
                Log.e("ViajesPlanner", "Error cargando outfits: " + e.getMessage());
            }
        }).start();
    }

    private void mostrarDialogoOutfits(List<OutfitFavorito> outfits, String nombreViaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Usamos el layout de lista de outfits (reutilizamos el del detalle si no tienes uno específico)
        // Asegúrate de tener: dialog_lista_outfits_viaje.xml
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lista_outfits_viaje, null);

        TextView txtTitulo = dialogView.findViewById(R.id.txtTituloOutfits);
        RecyclerView recycler = dialogView.findViewById(R.id.recyclerOutfitsViaje);

        if(txtTitulo != null) txtTitulo.setText("Outfits de " + nombreViaje);

        if (recycler != null) {
            recycler.setLayoutManager(new LinearLayoutManager(this));
            OutfitsViajeDetalleAdapter adapter = new OutfitsViajeDetalleAdapter(outfits);
            recycler.setAdapter(adapter);
        }

        builder.setView(dialogView)
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void eliminarViaje(Viaje viaje) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar viaje")
                .setMessage("¿Eliminar '" + viaje.nombre + "'?")
                .setPositiveButton("SÍ", (d, w) -> {
                    new Thread(() -> {
                        COMPLETO_AppDatabase.getDb(this).prendaDao().borrarViaje(viaje);
                        runOnUiThread(() -> Toast.makeText(this, "Viaje eliminado", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton("NO", null)
                .show();
    }

    // ========== ADAPTERS ==========

    private class OutfitsViajeAdapter extends RecyclerView.Adapter<OutfitsViajeAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            // Asegúrate de tener item_outfit_viaje.xml
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outfit_viaje, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            OutfitFavorito outfit = outfitsDelViaje.get(position);
            holder.txtDia.setText("Outfit " + (position + 1));
            holder.txtDetalle.setText(outfit.estilo);
        }

        @Override
        public int getItemCount() {
            return outfitsDelViaje.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtDia, txtDetalle;

            ViewHolder(View view) {
                super(view);
                txtDia = view.findViewById(R.id.txtDiaOutfit);
                txtDetalle = view.findViewById(R.id.txtPrendasOutfit);
            }
        }
    }

    // Adapter para mostrar el detalle de los outfits con imágenes
    private class OutfitsViajeDetalleAdapter extends RecyclerView.Adapter<OutfitsViajeDetalleAdapter.ViewHolder> {

        private List<OutfitFavorito> outfits;

        OutfitsViajeDetalleAdapter(List<OutfitFavorito> outfits) {
            this.outfits = outfits;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            // Asegúrate de tener item_outfit_viaje_detalle.xml
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outfit_viaje_detalle, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            OutfitFavorito outfit = outfits.get(position);
            holder.txtNombre.setText(outfit.nombre);

            new Thread(() -> {
                try {
                    // Cargar al menos la prenda superior para la preview
                    Prenda top = null;
                    if (outfit.idTop != 0) {
                        top = COMPLETO_AppDatabase.getDb(ViajesPlannerActivity.this).prendaDao().obtenerPorId(outfit.idTop);
                    }

                    Prenda pFinal = top;
                    runOnUiThread(() -> {
                        if (pFinal != null && pFinal.rutaImagen != null) {
                            File file = new File(pFinal.rutaImagen);
                            if (file.exists()) {
                                Glide.with(ViajesPlannerActivity.this)
                                        .load(file)
                                        .centerCrop()
                                        .into(holder.imgPreview);
                            }
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }

        @Override
        public int getItemCount() { return outfits.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre;
            ImageView imgPreview;

            ViewHolder(View view) {
                super(view);
                txtNombre = view.findViewById(R.id.txtNombreOutfitViaje);
                imgPreview = view.findViewById(R.id.imgPreviewOutfitViaje);
            }
        }
    }
}