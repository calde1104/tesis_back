package back.tesis.modules.dashboard;

import back.tesis.dto.context.CalendarioDto;
import back.tesis.dto.context.MacroDto;
import back.tesis.dto.dashboard.ContextVariablesDto;
import back.tesis.dto.dashboard.KpisDto;
import back.tesis.dto.dashboard.MarketShareCompositionDto;
import back.tesis.dto.dashboard.SalesEvolutionPointDto;
import back.tesis.dto.dashboard.ShareOfAssortmentCompositionDto;
import back.tesis.dto.sales.SalesSummaryDto;
import back.tesis.entities.Empresa;
import back.tesis.entities.EmpresaRepository;
import back.tesis.mapper.dashboard.DashboardMapper;
import back.tesis.modules.context.ContextService;
import back.tesis.modules.marketshare.MarketShareService;
import back.tesis.modules.sales.SalesService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int MESES_VENTANA_ACTIVO = 12;

    private final SalesService salesService;
    private final MarketShareService marketShareService;
    private final MarketShareCompositionRepository compositionRepository;
    private final EmpresaRepository empresaRepository;
    private final ContextService contextService;
    private final AssortmentRepository assortmentRepository;

    public DashboardService(
            SalesService salesService,
            MarketShareService marketShareService,
            MarketShareCompositionRepository compositionRepository,
            EmpresaRepository empresaRepository,
            ContextService contextService,
            AssortmentRepository assortmentRepository
    ) {
        this.salesService = salesService;
        this.marketShareService = marketShareService;
        this.compositionRepository = compositionRepository;
        this.empresaRepository = empresaRepository;
        this.contextService = contextService;
        this.assortmentRepository = assortmentRepository;
    }

    public KpisDto getKpis(String empresaId, int anio, int mes) {
        SalesSummaryDto summary = salesService.getSummary(empresaId, anio, mes);
        Double marketShareProm = marketShareService.getMarketShareUnidadesPromedioPorPeriodo(empresaId, anio, mes);
        Double priceGapProm = marketShareService.getPriceGapPctPromedioPorPeriodo(empresaId, anio, mes);
        Double shareOfAssortmentPct = calcularShareOfAssortment(empresaId, anio, mes, null, null, null);
        return DashboardMapper.toKpisDto(anio, mes, summary, marketShareProm, priceGapProm, shareOfAssortmentPct);
    }

    public List<ShareOfAssortmentCompositionDto> getShareOfAssortmentRanking(
            String empresaId, int anio, int mes, String marcaId, Integer categoriaId, Integer departamentoId) {
        int cutoffAnio = anio;
        int cutoffMes = mes - MESES_VENTANA_ACTIVO + 1;
        if (cutoffMes <= 0) {
            cutoffAnio -= 1;
            cutoffMes += 12;
        }

        List<String> productosActivosPropios = assortmentRepository.findActiveProductoIds(
                empresaId, cutoffAnio, cutoffMes, anio, mes, marcaId, categoriaId, departamentoId);
        if (productosActivosPropios.isEmpty()) {
            return List.of();
        }

        List<Integer> categoriaIds = assortmentRepository.findCategoriaIdsForProductos(productosActivosPropios);
        if (categoriaIds.isEmpty()) {
            return List.of();
        }

        long totalMercado = assortmentRepository.countMarketProductosByCategorias(categoriaIds);
        if (totalMercado == 0) {
            return List.of();
        }

        List<AssortmentRepository.EmpresaProductoCountRow> rows = assortmentRepository.countActiveProductosPorEmpresa(
                cutoffAnio, cutoffMes, anio, mes, categoriaIds, marcaId, departamentoId);

        List<String> empresaIds = rows.stream().map(AssortmentRepository.EmpresaProductoCountRow::getEmpresaId).toList();
        Map<String, String> nombresPorEmpresa = empresaRepository.findAllById(empresaIds).stream()
                .collect(Collectors.toMap(Empresa::getId, Empresa::getNombre));

        return rows.stream()
                .map(row -> new ShareOfAssortmentCompositionDto(
                        row.getEmpresaId(),
                        nombresPorEmpresa.getOrDefault(row.getEmpresaId(), "(desconocida)"),
                        100.0 * row.getTotalProductos() / totalMercado))
                .sorted(Comparator.comparing(
                        ShareOfAssortmentCompositionDto::shareOfAssortmentPct, Comparator.reverseOrder()))
                .toList();
    }

    private Double calcularShareOfAssortment(
            String empresaId, int anio, int mes, String marcaId, Integer categoriaId, Integer departamentoId) {
        int cutoffAnio = anio;
        int cutoffMes = mes - MESES_VENTANA_ACTIVO + 1;
        if (cutoffMes <= 0) {
            cutoffAnio -= 1;
            cutoffMes += 12;
        }

        List<String> productosActivos = assortmentRepository.findActiveProductoIds(
                empresaId, cutoffAnio, cutoffMes, anio, mes, marcaId, categoriaId, departamentoId);
        if (productosActivos.isEmpty()) {
            return null;
        }

        List<Integer> categoriaIds = assortmentRepository.findCategoriaIdsForProductos(productosActivos);
        if (categoriaIds.isEmpty()) {
            return null;
        }

        long totalMercado = assortmentRepository.countMarketProductosByCategorias(categoriaIds);
        if (totalMercado == 0) {
            return null;
        }

        return 100.0 * productosActivos.size() / totalMercado;
    }

    public List<SalesEvolutionPointDto> getSalesEvolution(String empresaId) {
        return salesService.getMonthlyRevenue(empresaId).stream()
                .map(DashboardMapper::toSalesEvolutionDto)
                .toList();
    }

    public List<MarketShareCompositionDto> getMarketShareComposition(
            String empresaId, int anio, int mes, String marcaId, Integer categoriaId, Integer departamentoId) {
        List<MarketShareCompositionRepository.CompositionRow> rows =
                compositionRepository.findComposicionPorPeriodo(empresaId, anio, mes, marcaId, categoriaId, departamentoId);

        List<String> empresaIds = rows.stream()
                .map(MarketShareCompositionRepository.CompositionRow::getEmpresaId)
                .toList();

        Map<String, String> nombresPorEmpresa = empresaRepository.findAllById(empresaIds).stream()
                .collect(Collectors.toMap(Empresa::getId, Empresa::getNombre));

        long totalMercado = rows.stream().mapToLong(MarketShareCompositionRepository.CompositionRow::getTotalUnidades).sum();

        return rows.stream()
                .map(row -> DashboardMapper.toCompositionDto(
                        row.getEmpresaId(),
                        nombresPorEmpresa.getOrDefault(row.getEmpresaId(), "(desconocida)"),
                        totalMercado == 0 ? null : 100.0 * row.getTotalUnidades() / totalMercado))
                .toList();
    }

    public ContextVariablesDto getContextVariables(String empresaId, int anio, int mes) {
        MacroDto macro = contextService.getMacro().stream()
                .filter(m -> m.anio() == anio && m.mes() == mes)
                .findFirst().orElse(null);
        CalendarioDto calendario = contextService.getCalendario().stream()
                .filter(c -> c.anio() == anio && c.mes() == mes)
                .findFirst().orElse(null);
        return DashboardMapper.toContextVariablesDto(macro, calendario);
    }
}
