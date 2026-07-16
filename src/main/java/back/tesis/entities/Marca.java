package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "marcas")
public class Marca extends UuidEntity {

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(nullable = false)
    private String nombre;

    protected Marca() {
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public String getNombre() {
        return nombre;
    }
}
