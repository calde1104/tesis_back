package back.tesis.entities;

public interface UsuarioAuth {

    String getId();

    String getEmpresaId();

    Integer getRolId();

    String getEmail();

    String getPasswordHash();

    boolean isActivo();
}
