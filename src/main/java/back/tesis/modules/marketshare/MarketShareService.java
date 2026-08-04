package back.tesis.modules.marketshare;

import back.tesis.dto.marketshare.CategoriaMarketShareSummaryDto;
import back.tesis.dto.marketshare.EmpresaShareDto;
import back.tesis.dto.marketshare.MarketShareOverviewPointDto;
import back.tesis.dto.marketshare.PriceGapDto;
import back.tesis.entities.Categoria;
import back.tesis.entities.Empresa;
import back.tesis.entities.EmpresaRepository;
import back.tesis.mapper.marketshare.MarketShareMapper;
import back.tesis.modules.sales.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MarketShareService {

    private final VentaMensualMarketShareRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;

    public MarketShareService(
            VentaMensualMarketShareRepository repository,
            CategoriaRepository categoriaRepository,
            EmpresaRepository empresaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.empresaRepository = empresaRepository;
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

    public List<CategoriaMarketShareSummaryDto> getCategoriesSummary(
            String empresaId, int anio, int mes, Integer departamentoId) {
        List<Integer> categoriaIds =
                repository.findCategoriaIdsDondeCompiteEmpresaEnPeriodo(empresaId, anio, mes, departamentoId);
        if (categoriaIds.isEmpty()) {
            return List.of();
        }

        List<VentaMensualMarketShareRepository.CategoriaEmpresaUnidadesRow> rows =
                repository.findUnidadesPorCategoriaYEmpresa(anio, mes, categoriaIds, departamentoId);
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<Integer, List<VentaMensualMarketShareRepository.CategoriaEmpresaUnidadesRow>> filasPorCategoria =
                rows.stream().collect(Collectors.groupingBy(
                        VentaMensualMarketShareRepository.CategoriaEmpresaUnidadesRow::getCategoriaId));

        Map<Integer, String> nombresPorCategoria = categoriaRepository.findAllById(categoriaIds).stream()
                .collect(Collectors.toMap(Categoria::getId, Categoria::getNombre));

        List<String> empresaIds = rows.stream()
                .map(VentaMensualMarketShareRepository.CategoriaEmpresaUnidadesRow::getEmpresaId)
                .distinct()
                .toList();
        Map<String, String> nombresPorEmpresa = empresaRepository.findAllById(empresaIds).stream()
                .collect(Collectors.toMap(Empresa::getId, Empresa::getNombre));

        return filasPorCategoria.entrySet().stream()
                .map(entry -> toCategoriaSummaryDto(
                        empresaId, nombresPorCategoria, nombresPorEmpresa, entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategoriaMarketShareSummaryDto::categoria))
                .toList();
    }

    private static CategoriaMarketShareSummaryDto toCategoriaSummaryDto(
            String empresaId,
            Map<Integer, String> nombresPorCategoria,
            Map<String, String> nombresPorEmpresa,
            Integer categoriaId,
            List<VentaMensualMarketShareRepository.CategoriaEmpresaUnidadesRow> filas) {
        long totalCategoria = filas.stream()
                .mapToLong(VentaMensualMarketShareRepository.CategoriaEmpresaUnidadesRow::getTotalUnidades)
                .sum();

        List<EmpresaShareDto> ranking = filas.stream()
                .map(fila -> new EmpresaShareDto(
                        nombresPorEmpresa.getOrDefault(fila.getEmpresaId(), "(desconocida)"),
                        totalCategoria == 0 ? null : 100.0 * fila.getTotalUnidades() / totalCategoria))
                .sorted(Comparator.comparing(
                        EmpresaShareDto::cuota, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        Double nuestraCuota = filas.stream()
                .filter(fila -> fila.getEmpresaId().equals(empresaId))
                .findFirst()
                .map(fila -> totalCategoria == 0 ? null : 100.0 * fila.getTotalUnidades() / totalCategoria)
                .orElse(null);

        return new CategoriaMarketShareSummaryDto(
                nombresPorCategoria.getOrDefault(categoriaId, "(desconocida)"), nuestraCuota, ranking);
    }
}
