package back.tesis.modules.marketshare;

import back.tesis.dto.marketshare.MarketShareOverviewPointDto;
import back.tesis.dto.marketshare.PriceGapDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/{empresaId}/market-share")
public class MarketShareController {

    private final MarketShareService marketShareService;

    public MarketShareController(MarketShareService marketShareService) {
        this.marketShareService = marketShareService;
    }

    @GetMapping("/overview")
    public List<MarketShareOverviewPointDto> getOverview(
            @PathVariable String empresaId,
            @RequestParam(required = false) String marcaId,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer departamentoId,
            @RequestParam(required = false) List<Integer> anios,
            @RequestParam(required = false) List<Integer> meses
    ) {
        return marketShareService.getOverview(empresaId, marcaId, categoriaId, departamentoId, anios, meses);
    }

    @GetMapping("/price-gap")
    public List<PriceGapDto> getPriceGap(
            @PathVariable String empresaId,
            @RequestParam(required = false) String marcaId,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer departamentoId,
            @RequestParam(required = false) List<Integer> anios,
            @RequestParam(required = false) List<Integer> meses
    ) {
        return marketShareService.getPriceGap(empresaId, marcaId, categoriaId, departamentoId, anios, meses);
    }
}
