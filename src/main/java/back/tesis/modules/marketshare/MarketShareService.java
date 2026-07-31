package back.tesis.modules.marketshare;

import back.tesis.dto.marketshare.MarketShareOverviewPointDto;
import back.tesis.dto.marketshare.PriceGapDto;
import back.tesis.mapper.marketshare.MarketShareMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MarketShareService {

    private final VentaMensualMarketShareRepository repository;

    public MarketShareService(VentaMensualMarketShareRepository repository) {
        this.repository = repository;
    }

    public List<MarketShareOverviewPointDto> getOverview(
            String empresaId, String marcaId, Integer categoriaId, Integer departamentoId,
            List<Integer> anios, List<Integer> meses) {
        List<VentaMensualMarketShareRepository.PeriodoUnidadesRow> filasEmpresa =
                repository.findUnidadesEmpresaPorPeriodo(empresaId, marcaId, categoriaId, departamentoId, anios, meses);
        Map<String, Long> unidadesMercadoPorPeriodo =
                repository.findUnidadesMercadoPorPeriodo(empresaId, marcaId, categoriaId, departamentoId, anios, meses)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> clavePeriodo(row.getAnio(), row.getMes()),
                                VentaMensualMarketShareRepository.PeriodoUnidadesRow::getTotalUnidades));

        return filasEmpresa.stream()
                .map(row -> {
                    Long unidadesMercado = unidadesMercadoPorPeriodo.get(clavePeriodo(row.getAnio(), row.getMes()));
                    Double pct = (unidadesMercado == null || unidadesMercado == 0)
                            ? null
                            : 100.0 * row.getTotalUnidades() / unidadesMercado;
                    return new MarketShareOverviewPointDto(row.getAnio(), row.getMes(), pct);
                })
                .toList();
    }

    private static String clavePeriodo(int anio, int mes) {
        return anio + "-" + mes;
    }

    public List<PriceGapDto> getPriceGap(
            String empresaId, String marcaId, Integer categoriaId, Integer departamentoId,
            List<Integer> anios, List<Integer> meses) {
        return repository.findUltimoPriceGapPorProducto(empresaId, marcaId, categoriaId, departamentoId, anios, meses)
                .stream()
                .map(MarketShareMapper::toDto)
                .toList();
    }

    public Double getMarketShareUnidadesPromedioPorPeriodo(String empresaId, int anio, int mes) {
        Long unidadesEmpresa = repository.sumUnidadesEmpresaPorPeriodo(empresaId, anio, mes);
        Long unidadesMercado = repository.sumUnidadesMercadoPorPeriodo(empresaId, anio, mes);
        if (unidadesEmpresa == null || unidadesMercado == null || unidadesMercado == 0) {
            return null;
        }
        return 100.0 * unidadesEmpresa / unidadesMercado;
    }

    public Double getPriceGapPctPromedioPorPeriodo(String empresaId, int anio, int mes) {
        return repository.findPriceGapPctPromedioPorPeriodo(empresaId, anio, mes);
    }
}
