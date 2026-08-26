package back.tesis.modules.notificaciones;

import back.tesis.common.PaginatedResponse;
import back.tesis.dto.notificaciones.NotificacionDto;
import back.tesis.security.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/{empresaId}/notificaciones")
public class NotificacionesController {

    private final NotificacionesService notificacionesService;

    public NotificacionesController(NotificacionesService notificacionesService) {
        this.notificacionesService = notificacionesService;
    }

    @GetMapping
    public PaginatedResponse<NotificacionDto> listar(@PathVariable String empresaId, Pageable pageable) {
        return PaginatedResponse.of(notificacionesService.listar(empresaId, pageable));
    }

    @PatchMapping("/{id}/vista")
    public NotificacionDto marcarVista(@PathVariable String empresaId, @PathVariable String id) {
        return notificacionesService.marcarVista(empresaId, id);
    }

    @PatchMapping("/{id}/resuelta")
    public NotificacionDto marcarResuelta(@PathVariable String empresaId, @PathVariable String id) {
        return notificacionesService.marcarResuelta(empresaId, id, TenantContext.get().usuarioId());
    }
}
