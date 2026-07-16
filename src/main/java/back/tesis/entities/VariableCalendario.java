package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "variables_calendario")
public class VariableCalendario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(name = "cantidad_feriados", nullable = false)
    private int cantidadFeriados;

    @Column(name = "tiene_festivo_comercial", nullable = false)
    private boolean tieneFestivoComercial;

    @Column(name = "nombre_festivo")
    private String nombreFestivo;

    @Column(name = "quincena_aguinaldo", nullable = false)
    private boolean quincenaAguinaldo;

    @Column(name = "inicio_ciclo_escolar", nullable = false)
    private boolean inicioCicloEscolar;

    @Column(name = "ciclo_cobro_mensual", nullable = false)
    private boolean cicloCobroMensual;

    protected VariableCalendario() {
    }

    public int getAnio() {
        return anio;
    }

    public int getMes() {
        return mes;
    }

    public int getCantidadFeriados() {
        return cantidadFeriados;
    }

    public boolean isTieneFestivoComercial() {
        return tieneFestivoComercial;
    }

    public String getNombreFestivo() {
        return nombreFestivo;
    }

    public boolean isQuincenaAguinaldo() {
        return quincenaAguinaldo;
    }

    public boolean isInicioCicloEscolar() {
        return inicioCicloEscolar;
    }

    public boolean isCicloCobroMensual() {
        return cicloCobroMensual;
    }
}
