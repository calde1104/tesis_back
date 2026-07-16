package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario extends UuidEntity {

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(name = "id_rol", nullable = false)
    private Integer rolId;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    protected Usuario() {
    }

    public Usuario(String empresaId, Integer rolId, String email, String passwordHash) {
        this.empresaId = empresaId;
        this.rolId = rolId;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public Integer getRolId() {
        return rolId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
