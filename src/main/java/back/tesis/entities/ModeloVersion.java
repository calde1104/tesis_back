package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "modelos_versiones")
public class ModeloVersion extends UuidEntity {

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(nullable = false)
    private String version;

    @Column(name = "fecha_entrenamiento", nullable = false)
    private LocalDateTime fechaEntrenamiento;

    @Column(name = "periodo_datos_desde", nullable = false)
    private LocalDate periodoDatosDesde;

    @Column(name = "periodo_datos_hasta", nullable = false)
    private LocalDate periodoDatosHasta;

    @Column(nullable = false)
    private float mae;

    @Column(nullable = false)
    private float rmse;

    @Column(nullable = false)
    private float mape;

    @Column(nullable = false, columnDefinition = "json")
    private String hiperparametros;

    @Column(nullable = false)
    private boolean activo;

    protected ModeloVersion() {
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getFechaEntrenamiento() {
        return fechaEntrenamiento;
    }

    public LocalDate getPeriodoDatosDesde() {
        return periodoDatosDesde;
    }

    public LocalDate getPeriodoDatosHasta() {
        return periodoDatosHasta;
    }

    public float getMae() {
        return mae;
    }

    public float getRmse() {
        return rmse;
    }

    public float getMape() {
        return mape;
    }

    public boolean isActivo() {
        return activo;
    }
}
