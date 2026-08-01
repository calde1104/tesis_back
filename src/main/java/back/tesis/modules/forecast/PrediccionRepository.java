package back.tesis.modules.forecast;

import back.tesis.dto.forecast.ForecastAgregadoRow;
import back.tesis.entities.Prediccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrediccionRepository extends JpaRepository<Prediccion, String> {

    @Query("""
            SELECT new back.tesis.dto.forecast.ForecastAgregadoRow(
                p.anio, p.mes, SUM(p.unidadesPredichas), SUM(p.intervaloConfianzaInf), SUM(p.intervaloConfianzaSup))
            FROM Prediccion p
            WHERE p.empresaId = :empresaId AND p.modeloId = :modeloId
            GROUP BY p.anio, p.mes
            ORDER BY p.anio, p.mes
            """)
    List<ForecastAgregadoRow> findForecastAgregado(
            @Param("empresaId") String empresaId,
            @Param("modeloId") String modeloId);
}
