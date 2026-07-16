package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "variables_climaticas")
public class VariableClimatica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(name = "temp_max_promedio")
    private Float tempMaxPromedio;

    @Column(name = "temp_media_promedio")
    private Float tempMediaPromedio;

    @Column(name = "precipitacion_mm")
    private Float precipitacionMm;

    protected VariableClimatica() {
    }

    public int getAnio() {
        return anio;
    }

    public int getMes() {
        return mes;
    }

    public Float getTempMaxPromedio() {
        return tempMaxPromedio;
    }

    public Float getTempMediaPromedio() {
        return tempMediaPromedio;
    }

    public Float getPrecipitacionMm() {
        return precipitacionMm;
    }
}
