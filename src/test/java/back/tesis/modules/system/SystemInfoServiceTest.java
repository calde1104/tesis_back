package back.tesis.modules.system;

import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.entities.Empresa;
import back.tesis.entities.EmpresaRepository;
import back.tesis.entities.ModeloVersion;
import back.tesis.modules.models.ModeloVersionRepository;
import back.tesis.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemInfoServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private ModeloVersionRepository modeloVersionRepository;
    @Mock
    private DataSource dataSource;

    private SystemInfoService service;

    @BeforeEach
    void setUp() {
        service = new SystemInfoService(empresaRepository, modeloVersionRepository, dataSource);
        TenantContext.set(new TenantContext.AuthenticatedUser("user-1", "emp-1", "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Empresa empresaDePrueba() {
        Empresa empresa = instantiate(Empresa.class);
        ReflectionTestUtils.setField(empresa, "id", "emp-1");
        ReflectionTestUtils.setField(empresa, "nombre", "Acme SA");
        ReflectionTestUtils.setField(empresa, "cuit", "80012345-6");
        return empresa;
    }

    private ModeloVersion modeloDePrueba() {
        ModeloVersion modelo = instantiate(ModeloVersion.class);
        ReflectionTestUtils.setField(modelo, "version", "v3");
        ReflectionTestUtils.setField(modelo, "mape", 12.5f);
        ReflectionTestUtils.setField(modelo, "mae", 3.1f);
        ReflectionTestUtils.setField(modelo, "rmse", 4.2f);
        ReflectionTestUtils.setField(modelo, "periodoDatosDesde", LocalDate.of(2023, 1, 1));
        ReflectionTestUtils.setField(modelo, "periodoDatosHasta", LocalDate.of(2024, 12, 1));
        ReflectionTestUtils.setField(modelo, "fechaEntrenamiento", LocalDateTime.of(2025, 1, 10, 8, 0));
        return modelo;
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void returnsEmpresaAndUsuarioDataFromRealSources() throws SQLException {
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresaDePrueba()));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.empty());
        when(dataSource.getConnection()).thenThrow(new SQLException("no conectado"));

        var result = service.getSystemInfo("emp-1");

        assertThat(result.razonSocial()).isEqualTo("Acme SA");
        assertThat(result.cuit()).isEqualTo("80012345-6");
        assertThat(result.tenantUuid()).isEqualTo("emp-1");
        assertThat(result.rolUsuario()).isEqualTo("ADMIN");
        assertThat(result.emailUsuario()).isNull();
        assertThat(result.frameworkBase()).isNull();
        assertThat(result.proximoCron()).isNull();
    }

    @Test
    void populatesModelFieldsFromActiveModeloVersion() throws SQLException {
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresaDePrueba()));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.of(modeloDePrueba()));
        when(dataSource.getConnection()).thenThrow(new SQLException("no conectado"));

        var result = service.getSystemInfo("emp-1");

        assertThat(result.versionModelo()).isEqualTo("v3");
        assertThat(result.mape()).isEqualTo(12.5f);
        assertThat(result.mae()).isEqualTo(3.1f);
        assertThat(result.rmse()).isEqualTo(4.2f);
        assertThat(result.rangoEntrenamiento()).isEqualTo("2023-01-01 - 2024-12-01");
        assertThat(result.ultimoReentrenamiento()).isEqualTo(LocalDateTime.of(2025, 1, 10, 8, 0));
    }

    @Test
    void leavesModelFieldsNullWhenEmpresaHasNoActiveModel() throws SQLException {
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresaDePrueba()));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.empty());
        when(dataSource.getConnection()).thenThrow(new SQLException("no conectado"));

        var result = service.getSystemInfo("emp-1");

        assertThat(result.versionModelo()).isNull();
        assertThat(result.mape()).isNull();
        assertThat(result.rangoEntrenamiento()).isNull();
        assertThat(result.ultimoReentrenamiento()).isNull();
    }

    @Test
    void reportsDbUpWithEngineAndLatencyWhenConnectionIsValid() throws SQLException {
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresaDePrueba()));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.empty());

        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("8.0.36");

        var result = service.getSystemInfo("emp-1");

        assertThat(result.dbStatus()).isEqualTo("UP");
        assertThat(result.dbEngine()).isEqualTo("MySQL 8.0.36");
        assertThat(result.dbLatencyMs()).isNotNull();
    }

    @Test
    void reportsDbDownWithNullEngineAndLatencyWhenConnectionFails() throws SQLException {
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.of(empresaDePrueba()));
        when(modeloVersionRepository.findByEmpresaIdAndActivoTrue("emp-1")).thenReturn(Optional.empty());
        when(dataSource.getConnection()).thenThrow(new SQLException("conexion rechazada"));

        var result = service.getSystemInfo("emp-1");

        assertThat(result.dbStatus()).isEqualTo("DOWN");
        assertThat(result.dbEngine()).isNull();
        assertThat(result.dbLatencyMs()).isNull();
    }

    @Test
    void throwsResourceNotFoundWhenEmpresaDoesNotExist() {
        when(empresaRepository.findById("emp-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSystemInfo("emp-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
