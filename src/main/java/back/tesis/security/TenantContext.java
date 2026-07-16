package back.tesis.security;

public final class TenantContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(AuthenticatedUser user) {
        CURRENT.set(user);
    }

    public static AuthenticatedUser get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public record AuthenticatedUser(String usuarioId, String empresaId, String rol) {
    }
}
