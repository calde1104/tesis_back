package back.tesis.modules.notificaciones;

import back.tesis.common.exceptions.ConflictException;
import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.entities.ConfigSistema;
import back.tesis.entities.EventoSistema;
import back.tesis.entities.ModeloVersion;
import back.tesis.entities.Prediccion;
import back.tesis.modules.config.ConfigSistemaRepository;
import back.tesis.modules.forecast.PrediccionRepository;
import back.tesis.modules.models.ModeloVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionesServiceTest {

    @Mock
    private ConfigSistemaRepository configRepository;
    @Mock
    private ModeloVersionRepository modeloVersionRepository;
    @Mock
    private PrediccionRepository prediccionRepository;
    @Mock
    private EventoSistemaRepository eventoRepository;

    private NotificacionesService newService() {
        return new NotificacionesService(configRepository, modeloVersionRepository,
                prediccionRepository, eventoRepository);
    }

    private ConfigSistema umbralConfig(String empresaId, String productoId, String valor) {
        return new ConfigSistema(empresaId, productoId, "UMBRAL_DEMANDA_MAXIMA", valor);
    }

    private static ModeloVersion modeloActivo(String id, String empresaId) throws ReflectiveOperationException {
        var constructor = ModeloVersion.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ModeloVersion modelo = constructor.newInstance();
        setField(modelo, ModeloVersion.class.getSuperclass(), "id", id);
        setField(modelo, ModeloVersion.class, "empresaId", empresaId);
        return modelo;
    }

    private static Prediccion prediccion(String empresaId, String productoId, int anio, int mes, float unidadesPredichas)
            throws ReflectiveOperationException {
        var constructor = Prediccion.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Prediccion p = constructor.newInstance();
        setField(p, Prediccion.class, "empresaId", empresaId);
        setField(p, Prediccion.class, "productoId", productoId);
        setField(p, Prediccion.class, "anio", anio);
        setField(p, Prediccion.class, "mes", mes);
        setField(p, Prediccion.class, "unidadesPredichas", unidadesPredichas);
        return p;
    }

    private static void setField(Object target, Class<?> declaringClass, String name, Object value)
            throws ReflectiveOperationException {
        Field field = declaringClass.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void generaAlertaCuandoPrediccionDelProximoMesSuperaElUmbral() throws Exception {
        when(configRepository.findByParametroAndProductoIdIsNotNull("UMBRAL_DEMANDA_MAXIMA"))
                .thenReturn(List.of(umbralConfig("emp-1", "prod-1", "400")));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1"))
                .thenReturn(Optional.of(modeloActivo("modelo-1", "emp-1")));
        when(prediccionRepository.findFirstByEmpresaIdAndProductoIdAndModeloIdOrderByAnioAscMesAsc(
                "emp-1", "prod-1", "modelo-1"))
                .thenReturn(Optional.of(prediccion("emp-1", "prod-1", 2026, 9, 410f)));
        when(eventoRepository.countByEmpresaIdAndProductoIdAndTipoEventoAndPeriodo(
                "emp-1", "prod-1", "UMBRAL_DEMANDA_SUPERADO", 2026, 9))
                .thenReturn(0L);

        newService().chequearUmbralesDemanda();

        ArgumentCaptor<EventoSistema> captor = ArgumentCaptor.forClass(EventoSistema.class);
        verify(eventoRepository).save(captor.capture());
        EventoSistema evento = captor.getValue();
        assertThat(evento.getEmpresaId()).isEqualTo("emp-1");
        assertThat(evento.getProductoId()).isEqualTo("prod-1");
        assertThat(evento.getTipoEvento()).isEqualTo("UMBRAL_DEMANDA_SUPERADO");
        assertThat(evento.getSeveridad()).isEqualTo(EventoSistema.SEVERIDAD_WARNING);
        assertThat(evento.getPayload()).contains("410").contains("2026").contains("9");
    }

    @Test
    void noGeneraAlertaCuandoPrediccionNoSuperaElUmbral() throws Exception {
        when(configRepository.findByParametroAndProductoIdIsNotNull("UMBRAL_DEMANDA_MAXIMA"))
                .thenReturn(List.of(umbralConfig("emp-1", "prod-1", "400")));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1"))
                .thenReturn(Optional.of(modeloActivo("modelo-1", "emp-1")));
        when(prediccionRepository.findFirstByEmpresaIdAndProductoIdAndModeloIdOrderByAnioAscMesAsc(
                "emp-1", "prod-1", "modelo-1"))
                .thenReturn(Optional.of(prediccion("emp-1", "prod-1", 2026, 9, 390f)));

        newService().chequearUmbralesDemanda();

        verify(eventoRepository, never()).save(any());
    }

    @Test
    void noDuplicaAlertaSiYaHayUnaParaEseProductoYPeriodoSinImportarElEstado() throws Exception {
        when(configRepository.findByParametroAndProductoIdIsNotNull("UMBRAL_DEMANDA_MAXIMA"))
                .thenReturn(List.of(umbralConfig("emp-1", "prod-1", "400")));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1"))
                .thenReturn(Optional.of(modeloActivo("modelo-1", "emp-1")));
        when(prediccionRepository.findFirstByEmpresaIdAndProductoIdAndModeloIdOrderByAnioAscMesAsc(
                "emp-1", "prod-1", "modelo-1"))
                .thenReturn(Optional.of(prediccion("emp-1", "prod-1", 2026, 9, 410f)));
        when(eventoRepository.countByEmpresaIdAndProductoIdAndTipoEventoAndPeriodo(
                "emp-1", "prod-1", "UMBRAL_DEMANDA_SUPERADO", 2026, 9))
                .thenReturn(1L);

        newService().chequearUmbralesDemanda();

        verify(eventoRepository, never()).save(any());
    }

    @Test
    void generaAlertaNuevaCuandoElPeriodoPronosticadoCambioAunConUnaAnteriorVista() throws Exception {
        when(configRepository.findByParametroAndProductoIdIsNotNull("UMBRAL_DEMANDA_MAXIMA"))
                .thenReturn(List.of(umbralConfig("emp-1", "prod-1", "400")));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1"))
                .thenReturn(Optional.of(modeloActivo("modelo-1", "emp-1")));
        when(prediccionRepository.findFirstByEmpresaIdAndProductoIdAndModeloIdOrderByAnioAscMesAsc(
                "emp-1", "prod-1", "modelo-1"))
                .thenReturn(Optional.of(prediccion("emp-1", "prod-1", 2026, 10, 420f)));
        when(eventoRepository.countByEmpresaIdAndProductoIdAndTipoEventoAndPeriodo(
                "emp-1", "prod-1", "UMBRAL_DEMANDA_SUPERADO", 2026, 10))
                .thenReturn(0L);

        newService().chequearUmbralesDemanda();

        verify(eventoRepository).save(any());
    }

    @Test
    void ignoraUmbralNoNumericoSinRomperElJob() {
        when(configRepository.findByParametroAndProductoIdIsNotNull("UMBRAL_DEMANDA_MAXIMA"))
                .thenReturn(List.of(umbralConfig("emp-1", "prod-1", "no-es-un-numero")));

        newService().chequearUmbralesDemanda();

        verify(modeloVersionRepository, never()).findByEmpresaIdAndActivoTrue(any());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void ignoraEmpresaSinModeloActivo() {
        when(configRepository.findByParametroAndProductoIdIsNotNull("UMBRAL_DEMANDA_MAXIMA"))
                .thenReturn(List.of(umbralConfig("emp-1", "prod-1", "400")));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.empty());

        newService().chequearUmbralesDemanda();

        verify(prediccionRepository, never())
                .findFirstByEmpresaIdAndProductoIdAndModeloIdOrderByAnioAscMesAsc(any(), any(), any());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void ignoraProductoSinPrediccionCargada() throws Exception {
        when(configRepository.findByParametroAndProductoIdIsNotNull("UMBRAL_DEMANDA_MAXIMA"))
                .thenReturn(List.of(umbralConfig("emp-1", "prod-1", "400")));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1"))
                .thenReturn(Optional.of(modeloActivo("modelo-1", "emp-1")));
        when(prediccionRepository.findFirstByEmpresaIdAndProductoIdAndModeloIdOrderByAnioAscMesAsc(
                eq("emp-1"), eq("prod-1"), eq("modelo-1")))
                .thenReturn(Optional.empty());

        newService().chequearUmbralesDemanda();

        verify(eventoRepository, never()).save(any());
    }

    @Test
    void reutilizaElModeloActivoCacheadoParaLaMismaEmpresaConVariosProductos() throws Exception {
        when(configRepository.findByParametroAndProductoIdIsNotNull("UMBRAL_DEMANDA_MAXIMA"))
                .thenReturn(List.of(
                        umbralConfig("emp-1", "prod-1", "400"),
                        umbralConfig("emp-1", "prod-2", "50")));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1"))
                .thenReturn(Optional.of(modeloActivo("modelo-1", "emp-1")));
        when(prediccionRepository.findFirstByEmpresaIdAndProductoIdAndModeloIdOrderByAnioAscMesAsc(any(), any(), any()))
                .thenReturn(Optional.empty());

        newService().chequearUmbralesDemanda();

        verify(modeloVersionRepository, org.mockito.Mockito.times(1)).findByEmpresaIdAndActivoTrue("emp-1");
    }

    private static EventoSistema evento(String estado) {
        EventoSistema evento = new EventoSistema("emp-1", "prod-1", "UMBRAL_DEMANDA_SUPERADO",
                EventoSistema.SEVERIDAD_WARNING, "{}");
        evento.setEstado(estado);
        return evento;
    }

    @Test
    void marcarVistaCambiaEstadoDeActivaAVista() {
        EventoSistema evento = evento(EventoSistema.ESTADO_ACTIVA);
        when(eventoRepository.findByIdAndEmpresaId("evt-1", "emp-1")).thenReturn(Optional.of(evento));
        when(eventoRepository.save(evento)).thenReturn(evento);

        newService().marcarVista("emp-1", "evt-1");

        assertThat(evento.getEstado()).isEqualTo(EventoSistema.ESTADO_VISTA);
    }

    @Test
    void marcarVistaFallaSiYaEstaResuelta() {
        EventoSistema evento = evento(EventoSistema.ESTADO_RESUELTA);
        when(eventoRepository.findByIdAndEmpresaId("evt-1", "emp-1")).thenReturn(Optional.of(evento));

        assertThatThrownBy(() -> newService().marcarVista("emp-1", "evt-1"))
                .isInstanceOf(ConflictException.class);
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void marcarVistaThrowsCuandoNoPerteneceALaEmpresa() {
        when(eventoRepository.findByIdAndEmpresaId("evt-1", "emp-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().marcarVista("emp-1", "evt-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void marcarResueltaSellaFechaYUsuario() {
        EventoSistema evento = evento(EventoSistema.ESTADO_ACTIVA);
        when(eventoRepository.findByIdAndEmpresaId("evt-1", "emp-1")).thenReturn(Optional.of(evento));
        when(eventoRepository.save(evento)).thenReturn(evento);

        newService().marcarResuelta("emp-1", "evt-1", "user-1");

        assertThat(evento.getEstado()).isEqualTo(EventoSistema.ESTADO_RESUELTA);
        assertThat(evento.getResueltoPor()).isEqualTo("user-1");
        assertThat(evento.getFechaResolucion()).isNotNull();
    }

    @Test
    void marcarResueltaEsIdempotenteSiYaEstabaResuelta() {
        EventoSistema evento = evento(EventoSistema.ESTADO_RESUELTA);
        when(eventoRepository.findByIdAndEmpresaId("evt-1", "emp-1")).thenReturn(Optional.of(evento));
        when(eventoRepository.save(evento)).thenReturn(evento);

        newService().marcarResuelta("emp-1", "evt-1", "user-1");

        assertThat(evento.getEstado()).isEqualTo(EventoSistema.ESTADO_RESUELTA);
        assertThat(evento.getResueltoPor()).isNull();
    }
}
