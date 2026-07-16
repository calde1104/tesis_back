package back.tesis.mapper.marketshare;

import back.tesis.dto.marketshare.MarketShareOverviewPointDto;
import back.tesis.dto.marketshare.PriceGapDto;
import back.tesis.modules.marketshare.VentaMensualMarketShareRepository;

public final class MarketShareMapper {

    private MarketShareMapper() {
    }

    public static MarketShareOverviewPointDto toDto(VentaMensualMarketShareRepository.OverviewRow row) {
        return new MarketShareOverviewPointDto(row.getAnio(), row.getMes(), row.getMarketShareUnidadesPromedio());
    }

    public static PriceGapDto toDto(VentaMensualMarketShareRepository.PriceGapRow row) {
        return new PriceGapDto(row.getProductoId(), row.getPrecioUnitario(), row.getPriceGapPct(),
                row.getAnio(), row.getMes());
    }
}
