package back.tesis.modules.marketshare;

import back.tesis.dto.marketshare.MarketShareOverviewPointDto;
import back.tesis.dto.marketshare.PriceGapDto;
import back.tesis.mapper.marketshare.MarketShareMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketShareService {

    private final VentaMensualMarketShareRepository repository;

    public MarketShareService(VentaMensualMarketShareRepository repository) {
        this.repository = repository;
    }

    public List<MarketShareOverviewPointDto> getOverview(
            String empresaId, String marcaId, Integer categoriaId, Integer departamentoId,
            List<Integer> anios, List<Integer> meses) {
        return repository.findMarketShareEvolucion(empresaId, marcaId, categoriaId, departamentoId, anios, meses)
                .stream()
                .map(MarketShareMapper::toDto)
                .toList();
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
        return repository.findMarketShareUnidadesPromedioPorPeriodo(empresaId, anio, mes);
    }

    public Double getPriceGapPctPromedioPorPeriodo(String empresaId, int anio, int mes) {
        return repository.findPriceGapPctPromedioPorPeriodo(empresaId, anio, mes);
    }
}
