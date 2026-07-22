package back.tesis.security;

import back.tesis.entities.LogAcceso;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

public class AccessLogInterceptor implements HandlerInterceptor {

    private final AccessLogService accessLogService;

    public AccessLogInterceptor(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex
    ) {
        TenantContext.AuthenticatedUser user = TenantContext.get();
        if (user == null) {
            return;
        }

        String resultado = resolverResultado(response.getStatus());
        accessLogService.registrar(
                user.usuarioId(), user.empresaId(), request.getMethod(), request.getRequestURI(),
                request.getRemoteAddr(), resultado);
    }

    private String resolverResultado(int status) {
        if (status < 400) {
            return LogAcceso.RESULTADO_OK;
        }
        if (status == 403) {
            return LogAcceso.RESULTADO_DENEGADO;
        }
        return LogAcceso.RESULTADO_ERROR;
    }
}
