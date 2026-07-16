package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "variables_macroeconomicas")
public class VariableMacroeconomica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(name = "ipc_indice")
    private Float ipcIndice;

    @Column(name = "ipc_var_interanual")
    private Float ipcVarInteranual;

    @Column(name = "tipo_cambio_pyg_usd")
    private Float tipoCambioPygUsd;

    @Column(name = "pbi_crecimiento")
    private Float pbiCrecimiento;

    @Column(name = "tasa_desempleo")
    private Float tasaDesempleo;

    @Column(name = "remesas_usd")
    private BigDecimal remesasUsd;

    @Column(name = "inversion_ext_usd")
    private BigDecimal inversionExtUsd;

    protected VariableMacroeconomica() {
    }

    public int getAnio() {
        return anio;
    }

    public int getMes() {
        return mes;
    }

    public Float getIpcIndice() {
        return ipcIndice;
    }

    public Float getIpcVarInteranual() {
        return ipcVarInteranual;
    }

    public Float getTipoCambioPygUsd() {
        return tipoCambioPygUsd;
    }

    public Float getPbiCrecimiento() {
        return pbiCrecimiento;
    }

    public Float getTasaDesempleo() {
        return tasaDesempleo;
    }

    public BigDecimal getRemesasUsd() {
        return remesasUsd;
    }

    public BigDecimal getInversionExtUsd() {
        return inversionExtUsd;
    }
}
