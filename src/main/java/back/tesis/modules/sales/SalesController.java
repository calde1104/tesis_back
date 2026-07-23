package back.tesis.modules.sales;

import back.tesis.common.PaginatedResponse;
import back.tesis.dto.sales.SaleItemDto;
import back.tesis.dto.sales.SalesFilterOptionsDto;
import back.tesis.dto.sales.SalesSummaryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/{empresaId}/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping
    public PaginatedResponse<SaleItemDto> listSales(
            @PathVariable String empresaId,
            @RequestParam List<Integer> anios,
            @RequestParam(required = false) List<Integer> meses,
            @RequestParam(required = false) String marcaId,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer departamentoId,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return PaginatedResponse.of(
                salesService.listSales(empresaId, anios, meses, marcaId, categoriaId, departamentoId, search, pageable));
    }

    @GetMapping("/summary")
    public SalesSummaryDto getSummary(
            @PathVariable String empresaId,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes
    ) {
        return salesService.getSummary(empresaId, anio, mes);
    }

    @GetMapping("/filters/options")
    public SalesFilterOptionsDto getFilterOptions(@PathVariable String empresaId) {
        return salesService.getFilterOptions(empresaId);
    }
}
