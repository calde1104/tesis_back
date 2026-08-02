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

        List<Integer> categoriaIds = resolverCategoriaIdsDeMercado(empresaId, marcaId, categoriaId, anios, meses);
        Map<String, Long> unidadesMercadoPorPeriodo = categoriaIds.isEmpty()
                ? Map.of()
                : repository.findUnidadesMercadoPorPeriodo(categoriaIds, departamentoId, anios, meses)
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

    private List<Integer> resolverCategoriaIdsDeMercado(
            String empresaId, String marcaId, Integer categoriaId, List<Integer> anios, List<Integer> meses) {
        if (marcaId != null) {
            return repository.findCategoriaIdsForMarca(marcaId);
        }
        if (categoriaId != null) {
            return List.of(categoriaId);
        }
        return repository.findCategoriaIdsDondeCompiteEmpresa(empresaId, anios, meses);
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
        if (unidadesEmpresa == null) {
            return null;
        }
        List<Integer> categoriaIds = repository.findCategoriaIdsDondeCompiteEmpresaEnPeriodo(empresaId, anio, mes);
        if (categoriaIds.isEmpty()) {
            return null;
        }
        Long unidadesMercado = repository.sumUnidadesMercadoPorPeriodo(anio, mes, categoriaIds);
        if (unidadesMercado == null || unidadesMercado == 0) {
            return null;
        }
        return 100.0 * unidadesEmpresa / unidadesMercado;
    }

    public Double getPriceGapPctPromedioPorPeriodo(String empresaId, int anio, int mes) {
        return repository.findPriceGapPctPromedioPorPeriodo(empresaId, anio, mes);
    }
}
