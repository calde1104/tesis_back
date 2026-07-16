package back.tesis.modules.dashboard;

import back.tesis.entities.VentaMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarketShareCompositionRepository extends JpaRepository<VentaMensual, Long> {

    interface CompositionRow {
        String getEmpresaId();
        Double getMarketShareUnidadesPromedio();
    }

    @Query("""
            SELECT v.empresaId AS empresaId, AVG(v.marketShareUnidades) AS marketShareUnidadesPromedio
            FROM VentaMensual v
            WHERE v.anio = :anio AND v.mes = :mes AND v.marketShareUnidades IS NOT NULL
            GROUP BY v.empresaId
            ORDER BY marketShareUnidadesPromedio DESC
            """)
    List<CompositionRow> findComposicionPorPeriodo(@Param("anio") int anio, @Param("mes") int mes);
}
