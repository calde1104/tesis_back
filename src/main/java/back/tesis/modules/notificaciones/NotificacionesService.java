package back.tesis.modules.notificaciones;

import back.tesis.common.exceptions.ConflictException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.dto.notificaciones.NotificacionDto;
import back.tesis.entities.ConfigSistema;
import back.tesis.entities.EventoSistema;
import back.tesis.entities.ModeloVersion;
import back.tesis.entities.Prediccion;
import back.tesis.mapper.notificaciones.NotificacionMapper;
import back.tesis.modules.config.ConfigSistemaRepository;
import back.tesis.modules.forecast.PrediccionRepository;
import back.tesis.modules.models.ModeloVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificacionesService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionesService.class);

    public static final String PARAMETRO_UMBRAL_DEMANDA = "UMBRAL_DEMANDA_MAXIMA";
    static final String TIPO_EVENTO_UMBRAL_DEMANDA = "UMBRAL_DEMANDA_SUPERADO";

    private final ConfigSistemaRepository configRepository;
    private final ModeloVersionRepository modeloVersionRepository;
    private final PrediccionRepository prediccionRepository;
    private final EventoSistemaRepository eventoRepository;

    public NotificacionesService(
            ConfigSistemaRepository configRepository,
            ModeloVersionRepository modeloVersionRepository,
            PrediccionRepository prediccionRepository,
            EventoSistemaRepository eventoRepository
    ) {
        this.configRepository = configRepository;
        this.modeloVersionRepository = modeloVersionRepository;
        this.prediccionRepository = prediccionRepository;
        this.eventoRepository = eventoRepository;
    }

    public Page<NotificacionDto> listar(String empresaId, Pageable pageable) {
        return eventoRepository.findByEmpresaIdOrderByFechaGeneracionDesc(empresaId, pageable)
                .map(NotificacionMapper::toDto);
    }

    public NotificacionDto marcarVista(String empresaId, String id) {
        EventoSistema evento = buscarDeLaEmpresa(empresaId, id);
        if (EventoSistema.ESTADO_RESUELTA.equals(evento.getEstado())) {
            throw new ConflictException("La notificacion ya esta resuelta, no se puede marcar como vista");
        }
        evento.setEstado(EventoSistema.ESTADO_VISTA);
        return NotificacionMapper.toDto(eventoRepository.save(evento));
    }

    public NotificacionDto marcarResuelta(String empresaId, String id, String usuarioId) {
        EventoSistema evento = buscarDeLaEmpresa(empresaId, id);
        if (!EventoSistema.ESTADO_RESUELTA.equals(evento.getEstado())) {
            evento.setEstado(EventoSistema.ESTADO_RESUELTA);
            evento.setFechaResolucion(LocalDateTime.now());
            evento.setResueltoPor(usuarioId);
        }
        return NotificacionMapper.toDto(eventoRepository.save(evento));
    }

    private EventoSistema buscarDeLaEmpresa(String empresaId, String id) {
        return eventoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada: " + id));
    }

    @Scheduled(cron = "${app.notificaciones.umbral-demanda-cron:0 0 6 * * *}")
    public void chequearUmbralesDemanda() {
        List<ConfigSistema> umbrales = configRepository.findByParametroAndProductoIdIsNotNull(PARAMETRO_UMBRAL_DEMANDA);
        Map<String, Optional<ModeloVersion>> modeloActivoPorEmpresa = new HashMap<>();

        int generadas = 0;
        for (ConfigSistema umbralConfig : umbrales) {
            if (procesarUmbral(umbralConfig, modeloActivoPorEmpresa)) {
                generadas++;
            }
        }
        log.info("Chequeo de umbrales de demanda: {} umbrales evaluados, {} alertas nuevas generadas",
                umbrales.size(), generadas);
    }

    public boolean chequearUmbralInmediato(ConfigSistema umbralConfig) {
        return procesarUmbral(umbralConfig, new HashMap<>());
    }

    private boolean procesarUmbral(ConfigSistema umbralConfig, Map<String, Optional<ModeloVersion>> modeloActivoPorEmpresa) {
        Double umbral = parsearUmbral(umbralConfig);
        if (umbral == null) {
            return false;
        }

        String empresaId = umbralConfig.getEmpresaId();
        String productoId = umbralConfig.getProductoId();

        Optional<ModeloVersion> modeloActivo = modeloActivoPorEmpresa
                .computeIfAbsent(empresaId, modeloVersionRepository::findByEmpresaIdAndActivoTrue);
        if (modeloActivo.isEmpty()) {
            return false;
        }

        Optional<Prediccion> proximaPrediccion = prediccionRepository
                .findFirstByEmpresaIdAndProductoIdAndModeloIdOrderByAnioAscMesAsc(
                        empresaId, productoId, modeloActivo.get().getId());
        if (proximaPrediccion.isEmpty()) {
            return false;
        }

        Prediccion prediccion = proximaPrediccion.get();
        if (prediccion.getUnidadesPredichas() < umbral) {
            return false;
        }

        Optional<Double> umbralDeLaUltimaAlerta = eventoRepository.findUmbralDeLaUltimaAlertaDelPeriodo(
                empresaId, productoId, TIPO_EVENTO_UMBRAL_DEMANDA, prediccion.getAnio(), prediccion.getMes());
        if (umbralDeLaUltimaAlerta.isPresent() && Math.abs(umbralDeLaUltimaAlerta.get() - umbral) < 0.0001) {
            return false;
        }

        eventoRepository.save(new EventoSistema(
                empresaId, productoId, TIPO_EVENTO_UMBRAL_DEMANDA, EventoSistema.SEVERIDAD_WARNING,
                construirPayload(umbral, prediccion)));
        return true;
    }

    private Double parsearUmbral(ConfigSistema umbralConfig) {
        try {
            return Double.parseDouble(umbralConfig.getValor().trim());
        } catch (NumberFormatException e) {
            log.warn("Umbral de demanda no numerico, se ignora (empresa={}, producto={}, valor='{}')",
                    umbralConfig.getEmpresaId(), umbralConfig.getProductoId(), umbralConfig.getValor());
            return null;
        }
    }

    private String construirPayload(double umbral, Prediccion prediccion) {
        return String.format(Locale.ROOT,
                "{\"umbralDemandaMaxima\":%s,\"unidadesPredichas\":%s,\"anio\":%d,\"mes\":%d}",
                umbral, prediccion.getUnidadesPredichas(), prediccion.getAnio(), prediccion.getMes());
    }
}
