package com.example.armariocamara;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarioActivity extends AppCompatActivity {

    private RecyclerView recyclerEventos;
    private Button btnAñadirEvento, btnHoy;
    private TextView txtFechaSeleccionada;
    private List<OutfitPlanificado> eventosDelDia = new ArrayList<>();
    private CalendarAdapter adapter;
    private String fechaSeleccionada;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_calendario);

            recyclerEventos = findViewById(R.id.recyclerEventos);
            btnAñadirEvento = findViewById(R.id.btnAñadirEvento);
            btnHoy = findViewById(R.id.btnHoy);
            txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada);

            recyclerEventos.setLayoutManager(new LinearLayoutManager(this));
            adapter = new CalendarAdapter(eventosDelDia, this::mostrarOpcionesEvento);
            recyclerEventos.setAdapter(adapter);

            // Seleccionar fecha de hoy
            fechaSeleccionada = sdf.format(Calendar.getInstance().getTime());
            actualizarFechaUI();
            cargarEventosDelDia();

            btnAñadirEvento.setOnClickListener(v -> seleccionarOutfitParaFecha());
            btnHoy.setOnClickListener(v -> irAHoy());
            txtFechaSeleccionada.setOnClickListener(v -> mostrarSelectorFecha());

        } catch (Exception e) {
            Log.e("CalendarioActivity", "Error en onCreate: " + e.getMessage());
            Toast.makeText(this, "Error al cargar calendario", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void mostrarSelectorFecha() {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            fechaSeleccionada = sdf.format(selected.getTime());
            actualizarFechaUI();
            cargarEventosDelDia();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void irAHoy() {
        fechaSeleccionada = sdf.format(Calendar.getInstance().getTime());
        actualizarFechaUI();
        cargarEventosDelDia();
    }

    private void actualizarFechaUI() {
        SimpleDateFormat displaySdf = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES"));
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(fechaSeleccionada));
            txtFechaSeleccionada.setText(displaySdf.format(cal.getTime()));
        } catch (Exception e) {
            txtFechaSeleccionada.setText(fechaSeleccionada);
        }
    }

    private void cargarEventosDelDia() {
        new Thread(() -> {
            try {
                eventosDelDia = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerOutfitsPorFecha(fechaSeleccionada);

                if (eventosDelDia == null) {
                    eventosDelDia = new ArrayList<>();
                }

                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();

                    if (eventosDelDia.isEmpty()) {
                        Toast.makeText(this, "No hay outfits para este día", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("CalendarioActivity", "Error cargando eventos: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error al cargar eventos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void seleccionarOutfitParaFecha() {
        new Thread(() -> {
            try {
                List<OutfitFavorito> favoritos = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerFavoritos();

                runOnUiThread(() -> {
                    if (favoritos == null || favoritos.isEmpty()) {
                        Toast.makeText(this, "No tienes outfits favoritos. Crea uno desde el armario.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    mostrarSelectorOutfit(favoritos);
                });

            } catch (Exception e) {
                Log.e("CalendarioActivity", "Error cargando favoritos: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error al cargar favoritos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrarSelectorOutfit(List<OutfitFavorito> favoritos) {
        String[] nombres = new String[favoritos.size()];
        for (int i = 0; i < favoritos.size(); i++) {
            nombres[i] = favoritos.get(i).nombre;
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona un outfit")
                .setItems(nombres, (d, which) -> {
                    OutfitFavorito seleccionado = favoritos.get(which);
                    añadirEventoAlCalendario(seleccionado);
                })
                .show();
    }

    private void añadirEventoAlCalendario(OutfitFavorito outfit) {
        EditText input = new EditText(this);
        input.setHint("Ocasión (ej: Reunión, Cena, etc.)");
        input.setTextColor(Color.BLACK);

        new AlertDialog.Builder(this)
                .setTitle("Detalles del evento")
                .setView(input)
                .setPositiveButton("GUARDAR", (d, w) -> {
                    String titulo = input.getText().toString().trim();
                    if (titulo.isEmpty()) titulo = outfit.nombre;

                    OutfitPlanificado evento = new OutfitPlanificado(
                            fechaSeleccionada,
                            titulo,
                            outfit.idTop,
                            outfit.idBottom,
                            outfit.idShoes,
                            outfit.idOuter,
                            System.currentTimeMillis(),
                            false
                    );

                    new Thread(() -> {
                        try {
                            COMPLETO_AppDatabase.getDb(this).prendaDao().agendarOutfit(evento);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "✅ Outfit añadido al calendario", Toast.LENGTH_SHORT).show();
                                cargarEventosDelDia();
                            });
                        } catch (Exception e) {
                            Log.e("CalendarioActivity", "Error guardando evento: " + e.getMessage());
                            runOnUiThread(() -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarOpcionesEvento(OutfitPlanificado evento) {
        String[] opciones = {"👗 Ver Outfit", "✅ Marcar como puesto", "❌ Eliminar"};

        new AlertDialog.Builder(this)
                .setTitle(evento.titulo)
                .setItems(opciones, (d, which) -> {
                    switch (which) {
                        case 0: verOutfitCompleto(evento); break;
                        case 1: marcarComoPuesto(evento); break;
                        case 2: eliminarEvento(evento); break;
                    }
                })
                .show();
    }

    private void marcarComoPuesto(OutfitPlanificado evento) {
        new Thread(() -> {
            try {
                // Marcar como puesto en la base de datos
                COMPLETO_AppDatabase.getDb(this).prendaDao().marcarComoPuesto(evento.id);

                // Registrar uso de cada prenda
                long fechaActual = System.currentTimeMillis();
                if (evento.idTop != 0) COMPLETO_AppDatabase.getDb(this).prendaDao().registrarUso(evento.idTop, fechaActual);
                if (evento.idBottom != 0) COMPLETO_AppDatabase.getDb(this).prendaDao().registrarUso(evento.idBottom, fechaActual);
                if (evento.idShoes != 0) COMPLETO_AppDatabase.getDb(this).prendaDao().registrarUso(evento.idShoes, fechaActual);
                if (evento.idOuter != 0) COMPLETO_AppDatabase.getDb(this).prendaDao().registrarUso(evento.idOuter, fechaActual);

                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Outfit marcado como puesto. Usos actualizados.", Toast.LENGTH_LONG).show();
                    cargarEventosDelDia();
                });

            } catch (Exception e) {
                Log.e("CalendarioActivity", "Error marcando como puesto: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void verOutfitCompleto(OutfitPlanificado evento) {
        new Thread(() -> {
            try {
                List<Prenda> prendas = new ArrayList<>();

                if (evento.idTop != 0) {
                    Prenda p = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorId(evento.idTop);
                    if (p != null) prendas.add(p);
                }
                if (evento.idBottom != 0) {
                    Prenda p = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorId(evento.idBottom);
                    if (p != null) prendas.add(p);
                }
                if (evento.idShoes != 0) {
                    Prenda p = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorId(evento.idShoes);
                    if (p != null) prendas.add(p);
                }
                if (evento.idOuter != 0) {
                    Prenda p = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerPorId(evento.idOuter);
                    if (p != null) prendas.add(p);
                }

                runOnUiThread(() -> mostrarDialogoOutfit(prendas, evento.titulo));

            } catch (Exception e) {
                Log.e("CalendarioActivity", "Error cargando outfit: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error al cargar outfit", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrarDialogoOutfit(List<Prenda> prendas, String titulo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_outfit_preview, null);

        TextView txtTitulo = dialogView.findViewById(R.id.txtTituloPreview);
        GridLayout gridPrendas = dialogView.findViewById(R.id.gridPrendasPreview);

        txtTitulo.setText(titulo);

        for (Prenda prenda : prendas) {
            ImageView img = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 200;
            params.height = 200;
            params.setMargins(8, 8, 8, 8);
            img.setLayoutParams(params);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (prenda.rutaImagen != null && new File(prenda.rutaImagen).exists()) {
                Glide.with(this)
                        .load(new File(prenda.rutaImagen))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img);
            }

            gridPrendas.addView(img);
        }

        builder.setView(dialogView)
                .setNegativeButton("Cerrar", null)
                .create()
                .show();
    }

    private void eliminarEvento(OutfitPlanificado evento) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar evento")
                .setMessage("¿Seguro que quieres eliminar '" + evento.titulo + "'?")
                .setPositiveButton("SÍ", (d, w) -> {
                    new Thread(() -> {
                        try {
                            COMPLETO_AppDatabase.getDb(this).prendaDao().borrarOutfitPlanificado(evento);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show();
                                cargarEventosDelDia();
                            });
                        } catch (Exception e) {
                            Log.e("CalendarioActivity", "Error eliminando: " + e.getMessage());
                            runOnUiThread(() -> Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("NO", null)
                .show();
    }
}
