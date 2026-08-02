package back.tesis.modules.dashboard;

import back.tesis.dto.dashboard.ContextVariablesDto;
import back.tesis.dto.dashboard.KpisDto;
import back.tesis.dto.dashboard.MarketShareCompositionDto;
import back.tesis.dto.dashboard.SalesEvolutionPointDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/{empresaId}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpis")
    public KpisDto getKpis(@PathVariable String empresaId, @RequestParam int anio, @RequestParam int mes) {
        return dashboardService.getKpis(empresaId, anio, mes);
    }

    @GetMapping("/sales-evolution")
    public List<SalesEvolutionPointDto> getSalesEvolution(@PathVariable String empresaId) {
        return dashboardService.getSalesEvolution(empresaId);
    }

    @GetMapping("/market-share-composition")
    public List<MarketShareCompositionDto> getMarketShareComposition(
            @PathVariable String empresaId, @RequestParam int anio, @RequestParam int mes,
            @RequestParam(required = false) String marcaId,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer departamentoId) {
        return dashboardService.getMarketShareComposition(empresaId, anio, mes, marcaId, categoriaId, departamentoId);
    }

    @GetMapping("/share-of-assortment")
    public List<?> getShareOfAssortment(
            @PathVariable String empresaId, @RequestParam int anio, @RequestParam int mes,
            @RequestParam(required = false) String marcaId,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer departamentoId,
            @RequestParam(required = false) Integer anioDesde) {
        return dashboardService.getShareOfAssortmentRanking(
                empresaId, anio, mes, marcaId, categoriaId, departamentoId, anioDesde);
    }

    @GetMapping("/context-variables")
    public ContextVariablesDto getContextVariables(
            @PathVariable String empresaId, @RequestParam int anio, @RequestParam int mes) {
        return dashboardService.getContextVariables(empresaId, anio, mes);
    }
}
