package com.example.armariocamara;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class FormularioIAActivity extends AppCompatActivity {

    private Spinner spinnerOcasion;
    private SeekBar seekPiezas, seekHoras, seekTemperatura;
    private TextView txtPiezas, txtHoras, txtTemperatura;
    private RadioGroup rgInteriorExterior;
    private CheckBox chkAbrigo, chkAccesorios, chkLluvia, chkMuchoSol;
    private ChipGroup chipGroupColores;
    private Button btnGenerar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_ia);

        inicializarVistas();
        configurarSpinner();
        configurarSeekBars();
        configurarChips();
        configurarRadioGroup();

        // Llamamos a la validación antes de generar
        btnGenerar.setOnClickListener(v -> validarYGenerarOutfit());
    }

    private void inicializarVistas() {
        spinnerOcasion = findViewById(R.id.spinnerOcasionIA);
        seekPiezas = findViewById(R.id.seekPiezas);
        seekHoras = findViewById(R.id.seekHoras);
        seekTemperatura = findViewById(R.id.seekTemperatura);
        txtPiezas = findViewById(R.id.txtPiezasSeleccionadas);
        txtHoras = findViewById(R.id.txtHorasSeleccionadas);
        txtTemperatura = findViewById(R.id.txtTemperaturaSeleccionada);
        rgInteriorExterior = findViewById(R.id.rgInteriorExterior);
        chkAbrigo = findViewById(R.id.chkNecesitaAbrigo);
        chkAccesorios = findViewById(R.id.chkNecesitaAccesorios);
        chkLluvia = findViewById(R.id.chkPosibilidadLluvia);
        chkMuchoSol = findViewById(R.id.chkMuchoSol);
        chipGroupColores = findViewById(R.id.chipGroupColores);
        btnGenerar = findViewById(R.id.btnGenerarOutfitIA);
    }

    private void configurarSpinner() {
        String[] ocasiones = getResources().getStringArray(R.array.ocasiones);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ocasiones) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(18);
                tv.setBackgroundColor(Color.WHITE);
                tv.setPadding(16, 16, 16, 16);
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) v;
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextSize(16);
                tv.setPadding(20, 20, 20, 20);
                return v;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOcasion.setAdapter(adapter);
    }

    private void configurarSeekBars() {
        seekPiezas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int piezas = progress + 2;
                txtPiezas.setText(piezas + " piezas");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekHoras.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int horas = progress + 1;
                txtHoras.setText(horas + " horas");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekTemperatura.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtTemperatura.setText(progress + "°C");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void configurarChips() {
        for (String color : DatosRopa.COLORES) {
            Chip chip = new Chip(this);
            chip.setText(color);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setTextColor(Color.BLACK);
            chipGroupColores.addView(chip);
        }
    }

    private void configurarRadioGroup() {
        rgInteriorExterior.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbExterior) {
                chkMuchoSol.setVisibility(View.VISIBLE);
            } else {
                chkMuchoSol.setVisibility(View.GONE);
                chkMuchoSol.setChecked(false);
            }
        });
    }

    // MÉTODO DE VALIDACIÓN SOLICITADO
    private void validarYGenerarOutfit() {
        Toast.makeText(this, "Validando prendas...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                // Obtener todas las prendas
                List<Prenda> todasLasPrendas = COMPLETO_AppDatabase.getDb(this)
                        .prendaDao()
                        .obtenerTodasPrendas();

                // 1. Validar si el armario está vacío
                if (todasLasPrendas == null || todasLasPrendas.isEmpty()) {
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(this)
                                .setTitle("Armario vacío")
                                .setMessage("No tienes prendas en tu armario. Añade algunas prendas primero.")
                                .setPositiveButton("Añadir prendas", (d, w) -> {
                                    Intent intent = new Intent(this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .setNegativeButton("Cancelar", (d, w) -> finish())
                                .show();
                    });
                    return;
                }

                // 2. Contar prendas por categoría
                int countTops = 0, countBottoms = 0, countShoes = 0;

                for (Prenda p : todasLasPrendas) {
                    // Usamos toLowerCase para evitar problemas de mayúsculas
                    String categoria = p.categoria != null ? p.categoria.toLowerCase() : "";

                    switch (categoria) {
                        case "parte superior": // Estándar de la app
                        case "camiseta":
                        case "camisa":
                        case "blusa":
                        case "sudadera":
                        case "top":
                        case "jersey":
                        case "chaqueta":
                        case "abrigo":
                            countTops++;
                            break;

                        case "parte inferior": // Estándar de la app
                        case "pantalon":
                        case "falda":
                        case "short":
                        case "vaquero":
                        case "jeans":
                            countBottoms++;
                            break;

                        case "calzado": // Estándar de la app
                        case "zapatillas":
                        case "zapatos":
                        case "botas":
                        case "sandalias":
                        case "deportivas":
                            countShoes++;
                            break;
                    }
                }

                // 3. Validar si hay suficientes prendas para un outfit básico
                if (countTops == 0 || countBottoms == 0 || countShoes == 0) {
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(this)
                                .setTitle("Prendas insuficientes")
                                .setMessage("Para generar un outfit completo necesitas al menos:\n" +
                                        "- 1 prenda superior (camiseta, camisa, etc.)\n" +
                                        "- 1 prenda inferior (pantalón, falda, etc.)\n" +
                                        "- 1 calzado\n\n" +
                                        "¿Deseas generar un outfit parcial con lo que tienes?")
                                .setPositiveButton("Sí, generar", (d, w) -> generarOutfit())
                                .setNegativeButton("No, añadir más prendas", (d, w) -> {
                                    Intent intent = new Intent(this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                    });
                } else {
                    // Todo correcto, procedemos a generar
                    runOnUiThread(() -> generarOutfit());
                }

            } catch (Exception e) {
                Log.e("FormularioIA", "Error validando prendas: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al validar prendas", Toast.LENGTH_SHORT).show();
                    finish(); // Opcional: cerrar si hay error crítico
                });
            }
        }).start();
    }

    private void generarOutfit() {
        String ocasion = spinnerOcasion.getSelectedItem().toString();
        int numPiezas = seekPiezas.getProgress() + 2;
        int duracionHoras = seekHoras.getProgress() + 1;
        double temperatura = seekTemperatura.getProgress();
        boolean esExterior = rgInteriorExterior.getCheckedRadioButtonId() == R.id.rbExterior;
        boolean necesitaAccesorios = chkAccesorios.isChecked();
        boolean posibleLluvia = chkLluvia.isChecked();
        boolean muchoSol = chkMuchoSol.isChecked();
        boolean haceFrio = temperatura < 15;

        List<String> coloresPreferidos = new ArrayList<>();
        for (int i = 0; i < chipGroupColores.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupColores.getChildAt(i);
            if (chip.isChecked()) {
                coloresPreferidos.add(chip.getText().toString());
            }
        }

        new Thread(() -> {
            // Nota: Aquí usamos obtenerLimpias() para generar solo con ropa disponible
            List<Prenda> armario = COMPLETO_AppDatabase.getDb(this).prendaDao().obtenerLimpias();
            Estilista estilista = new Estilista();

            List<Prenda> outfit = estilista.generarConjuntoProAvanzado(
                    armario, ocasion, null, new ArrayList<>(), esExterior, haceFrio,
                    necesitaAccesorios, numPiezas, temperatura, duracionHoras,
                    posibleLluvia, coloresPreferidos, muchoSol
            );

            runOnUiThread(() -> {
                if (outfit.isEmpty()) {
                    Toast.makeText(this, "No se pudo generar un outfit válido.\nIntenta cambiar los filtros o lava tu ropa.", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(this, VisualizadorOutfitActivity.class);
                    intent.putExtra("OUTFIT_IDS", obtenerIds(outfit));
                    startActivity(intent);
                }
            });
        }).start();
    }

    private int[] obtenerIds(List<Prenda> prendas) {
        int[] ids = new int[Math.min(4, prendas.size())];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = prendas.get(i).id;
        }
        return ids;
    }
}