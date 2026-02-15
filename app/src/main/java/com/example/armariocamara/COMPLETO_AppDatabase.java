package com.example.armariocamara;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Prenda.class, OutfitFavorito.class, OutfitPlanificado.class, Viaje.class},
        version = 5,  // INCREMENTADO de 4 a 5 debido a cambios en esquema
        exportSchema = false
)
public abstract class COMPLETO_AppDatabase extends RoomDatabase {

    public abstract COMPLETO_PrendaDao prendaDao();

    private static COMPLETO_AppDatabase INSTANCE;

    public static synchronized COMPLETO_AppDatabase getDb(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            COMPLETO_AppDatabase.class,
                            "armario_database"
                    )
                    .fallbackToDestructiveMigration()  // IMPORTANTE: Recreará la BD con el nuevo esquema
                    .build();
        }
        return INSTANCE;
    }
}