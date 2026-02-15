package com.example.armariocamara;

import androidx.room.*;
import java.util.List;

@Dao
public interface COMPLETO_PrendaDao {

    // ========== PRENDAS (CRUD BÁSICO) ==========

    // Insertar prenda (usado en GuardarPrendaActivity)
    @Insert
    long insertarPrenda(Prenda prenda);

    // Actualizar prenda (usado en EditarPrendaActivity)
    @Update
    void actualizar(Prenda prenda);

    // Borrar prenda
    @Delete
    void borrar(Prenda prenda);

    // Obtener TODAS (usado en ArmarioActivity, MainActivity para validaciones)
    @Query("SELECT * FROM prendas ORDER BY id DESC")
    List<Prenda> obtenerTodasPrendas();

    // Obtener SOLO LIMPIAS (usado en Generador de Outfits)
    @Query("SELECT * FROM prendas WHERE enLavanderia = 0 ORDER BY id DESC")
    List<Prenda> obtenerLimpias();

    // Obtener por ID (usado en Visualizador, Detalles)
    @Query("SELECT * FROM prendas WHERE id = :id")
    Prenda obtenerPorId(int id);

    // ========== FILTROS Y CONSULTAS ESPECÍFICAS ==========

    @Query("SELECT * FROM prendas WHERE categoria = :categoria AND enLavanderia = 0")
    List<Prenda> obtenerPorCategoria(String categoria);

    @Query("SELECT * FROM prendas WHERE enLavanderia = 1")
    List<Prenda> obtenerEnLavanderia();

    // Obtener marcas únicas para filtros
    @Query("SELECT DISTINCT marca FROM prendas WHERE marca IS NOT NULL AND marca != '' ORDER BY marca ASC")
    List<String> obtenerTodasLasMarcas();

    // ========== ACCIONES RÁPIDAS ==========

    @Query("UPDATE prendas SET enLavanderia = :estado WHERE id = :id")
    void actualizarLavanderia(int id, boolean estado);

    @Query("UPDATE prendas SET vecesUsada = vecesUsada + 1, ultimoUso = :fechaUso WHERE id = :id")
    void registrarUso(int id, long fechaUso);

    // ========== ESTADÍSTICAS ==========

    @Query("SELECT COUNT(*) FROM prendas")
    int contarPrendas();

    @Query("SELECT SUM(vecesUsada) FROM prendas")
    int contarUsosTotal();

    @Query("SELECT * FROM prendas ORDER BY vecesUsada DESC LIMIT 3")
    List<Prenda> obtenerTopUsadas();

    @Query("SELECT * FROM prendas WHERE esFavorito = 1 ORDER BY id DESC")
    List<Prenda> obtenerPrendasFavoritas();

    // Prendas olvidadas (sin uso o uso muy antiguo)
    @Query("SELECT * FROM prendas WHERE vecesUsada = 0 OR ultimoUso < :fechaLimite ORDER BY ultimoUso ASC LIMIT 5")
    List<Prenda> obtenerOlvidadas(long fechaLimite);

    // ========== OUTFITS FAVORITOS (MIS LOOKS) ==========

    @Insert
    long guardarFavorito(OutfitFavorito outfit);

    @Update
    void actualizarFavorito(OutfitFavorito outfit);

    @Delete
    void borrarFavorito(OutfitFavorito outfit);

    @Query("SELECT * FROM outfits_favoritos ORDER BY fechaCreacion DESC")
    List<OutfitFavorito> obtenerFavoritos();

    @Query("SELECT * FROM outfits_favoritos WHERE id = :id")
    OutfitFavorito obtenerFavoritoPorId(int id);

    // ========== CALENDARIO / OUTFITS PLANIFICADOS ==========

    // Agendar nuevo outfit (usado en Visualizador)
    @Insert
    long agendarOutfit(OutfitPlanificado outfit);

    @Update
    void actualizarOutfitPlanificado(OutfitPlanificado outfit);

    @Delete
    void borrarOutfitPlanificado(OutfitPlanificado outfit);

    // Obtener eventos de un día específico
    @Query("SELECT * FROM outfits_planificados WHERE fecha = :fecha")
    List<OutfitPlanificado> obtenerOutfitsPorFecha(String fecha);

    // Obtener eventos en un rango (para marcar puntitos en calendario)
    @Query("SELECT * FROM outfits_planificados WHERE fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha")
    List<OutfitPlanificado> obtenerOutfitsEnRango(String fechaInicio, String fechaFin);

    @Query("SELECT * FROM outfits_planificados WHERE id = :id")
    OutfitPlanificado obtenerOutfitPlanificadoPorId(int id);

    @Query("SELECT * FROM outfits_planificados WHERE fecha >= :fechaHoy ORDER BY fecha ASC")
    List<OutfitPlanificado> obtenerOutfitsFuturos(String fechaHoy);

    @Query("UPDATE outfits_planificados SET puesto = 1 WHERE id = :id")
    void marcarComoPuesto(int id);

    // ========== VIAJES ==========

    @Insert
    long guardarViaje(Viaje viaje);

    @Update
    void actualizarViaje(Viaje viaje);

    @Delete
    void borrarViaje(Viaje viaje);

    @Query("SELECT * FROM viajes ORDER BY fechaCreacion DESC")
    List<Viaje> obtenerViajes();

    @Query("SELECT * FROM viajes WHERE id = :id")
    Viaje obtenerViajePorId(int id);

    @Query("DELETE FROM viajes WHERE id = :id")
    void borrarViajePorId(int id);
}