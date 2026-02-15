package com.example.armariocamara;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class FiltrosDialog extends Dialog {

    public static class Filtros {
        public List<String> estilosSeleccionados = new ArrayList<>();
        public List<String> coloresSeleccionados = new ArrayList<>();
        public boolean soloLimpias = false;
        public boolean soloSucias = false;
    }

    public interface OnFiltrosAplicados {
        void onAplicar(Filtros filtros);
    }

    private OnFiltrosAplicados listener;
    private Filtros filtrosActuales = new Filtros();

    // Views
    private LinearLayout containerEstilos, containerColores;
    private CheckBox checkSoloLimpias, checkSoloSucias;
    private Button btnAplicar, btnLimpiar;

    public FiltrosDialog(@NonNull Context context, OnFiltrosAplicados listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_filtros);

        if (getWindow() != null) {
            int width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        containerEstilos = findViewById(R.id.containerEstilos);
        containerColores = findViewById(R.id.containerColores);
        checkSoloLimpias = findViewById(R.id.checkSoloLimpias);
        checkSoloSucias = findViewById(R.id.checkSoloSucias);
        btnAplicar = findViewById(R.id.btnAplicarFiltros);
        btnLimpiar = findViewById(R.id.btnLimpiarFiltros);

        cargarEstilos();
        cargarColores();

        // Evitar que ambos checkboxes estén marcados
        checkSoloLimpias.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkSoloSucias.setChecked(false);
        });
        checkSoloSucias.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkSoloLimpias.setChecked(false);
        });

        btnAplicar.setOnClickListener(v -> aplicarFiltros());
        btnLimpiar.setOnClickListener(v -> limpiarFiltros());
    }

    private void cargarEstilos() {
        String[] estilos = getContext().getResources().getStringArray(R.array.estilos_completo);

        for (String estilo : estilos) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(estilo);
            checkBox.setTextSize(14);
            containerEstilos.addView(checkBox);
        }
    }

    private void cargarColores() {
        String[] colores = {"Negro", "Blanco", "Gris", "Rojo", "Azul", "Verde",
                "Amarillo", "Naranja", "Rosa", "Morado", "Marrón", "Beige"};

        for (String color : colores) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(color);
            checkBox.setTextSize(14);
            containerColores.addView(checkBox);
        }
    }

    private void aplicarFiltros() {
        filtrosActuales = new Filtros();

        // Recoger estilos seleccionados
        for (int i = 0; i < containerEstilos.getChildCount(); i++) {
            View child = containerEstilos.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    filtrosActuales.estilosSeleccionados.add(checkBox.getText().toString());
                }
            }
        }

        // Recoger colores seleccionados
        for (int i = 0; i < containerColores.getChildCount(); i++) {
            View child = containerColores.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    filtrosActuales.coloresSeleccionados.add(checkBox.getText().toString());
                }
            }
        }

        filtrosActuales.soloLimpias = checkSoloLimpias.isChecked();
        filtrosActuales.soloSucias = checkSoloSucias.isChecked();

        if (listener != null) {
            listener.onAplicar(filtrosActuales);
        }

        dismiss();
    }

    private void limpiarFiltros() {
        // Desmarcar todos los checkboxes
        for (int i = 0; i < containerEstilos.getChildCount(); i++) {
            View child = containerEstilos.getChildAt(i);
            if (child instanceof CheckBox) {
                ((CheckBox) child).setChecked(false);
            }
        }

        for (int i = 0; i < containerColores.getChildCount(); i++) {
            View child = containerColores.getChildAt(i);
            if (child instanceof CheckBox) {
                ((CheckBox) child).setChecked(false);
            }
        }

        checkSoloLimpias.setChecked(false);
        checkSoloSucias.setChecked(false);

        Toast.makeText(getContext(), "Filtros limpiados", Toast.LENGTH_SHORT).show();
    }
}