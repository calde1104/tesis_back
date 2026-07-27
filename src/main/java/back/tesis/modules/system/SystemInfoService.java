package back.tesis.modules.system;

import back.tesis.common.exceptions.ResourceNotFoundException;
import back.tesis.dto.system.SystemInfoDto;
import back.tesis.entities.Empresa;
import back.tesis.entities.EmpresaRepository;
import back.tesis.entities.ModeloVersion;
import back.tesis.modules.models.ModeloVersionRepository;
import back.tesis.security.TenantContext;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Service
public class SystemInfoService {

    private static final int DB_PING_TIMEOUT_SECONDS = 2;

    private final EmpresaRepository empresaRepository;
    private final ModeloVersionRepository modeloVersionRepository;
    private final DataSource dataSource;

    public SystemInfoService(
            EmpresaRepository empresaRepository,
            ModeloVersionRepository modeloVersionRepository,
            DataSource dataSource
    ) {
        this.empresaRepository = empresaRepository;
        this.modeloVersionRepository = modeloVersionRepository;
        this.dataSource = dataSource;
    }

    public SystemInfoDto getSystemInfo(String empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + empresaId));

        TenantContext.AuthenticatedUser user = TenantContext.get();
        ModeloVersion modelo = modeloVersionRepository.findByEmpresaIdAndActivoTrue(empresaId).orElse(null);
        DbHealth dbHealth = checkDbHealth();

        return new SystemInfoDto(
                empresa.getNombre(),
                empresa.getCuit(),
                empresa.getId(),
                user.rol(),
                null,
                null,
                modelo == null ? null : modelo.getVersion(),
                modelo == null ? null : modelo.getMape(),
                modelo == null ? null : modelo.getMae(),
                modelo == null ? null : modelo.getRmse(),
                modelo == null ? null : modelo.getPeriodoDatosDesde() + " - " + modelo.getPeriodoDatosHasta(),
                modelo == null ? null : modelo.getFechaEntrenamiento(),
                null,
                dbHealth.engine(),
                dbHealth.status(),
                dbHealth.latencyMs()
        );
    }

    private DbHealth checkDbHealth() {
        long start = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(DB_PING_TIMEOUT_SECONDS);
            int latencyMs = (int) (System.currentTimeMillis() - start);
            DatabaseMetaData metaData = connection.getMetaData();
            String engine = metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion();
            return new DbHealth(engine, valid ? "UP" : "DOWN", latencyMs);
        } catch (SQLException e) {
            return new DbHealth(null, "DOWN", null);
        }
    }

    private record DbHealth(String engine, String status, Integer latencyMs) {
    }
}
