package back.tesis.security;

import back.tesis.common.exceptions.TenantMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

public class TenantGuardInterceptor implements HandlerInterceptor {

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables == null || !pathVariables.containsKey("empresaId")) {
            return true;
        }
        String pathEmpresaId = pathVariables.get("empresaId");
        TenantContext.AuthenticatedUser user = TenantContext.get();
        if (user == null || !user.empresaId().equals(pathEmpresaId)) {
            throw new TenantMismatchException("La empresa del path no coincide con la empresa autenticada");
        }
        return true;
    }
}
