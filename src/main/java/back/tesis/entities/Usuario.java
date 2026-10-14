package back.tesis.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario extends UuidEntity implements UsuarioAuth {

    @Column(name = "id_empresa", nullable = false)
    private String empresaId;

    @Column(name = "id_rol", nullable = false)
    private Integer rolId;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "foto_perfil", length = 16_777_215)
    private byte[] fotoPerfil;

    @Column(name = "foto_perfil_tipo")
    private String fotoPerfilTipo;

    @Column(nullable = false)
    private boolean activo = true;

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

    public byte[] getFotoPerfil() {
        return fotoPerfil;
    }

    public String getFotoPerfilTipo() {
        return fotoPerfilTipo;
    }

    public void actualizarFoto(byte[] fotoPerfil, String fotoPerfilTipo) {
        this.fotoPerfil = fotoPerfil;
        this.fotoPerfilTipo = fotoPerfilTipo;
    }

    public void quitarFoto() {
        this.fotoPerfil = null;
        this.fotoPerfilTipo = null;
    }

    public boolean isActivo() {
        return activo;
    }

    public void baja() {
        this.activo = false;
    }
}
