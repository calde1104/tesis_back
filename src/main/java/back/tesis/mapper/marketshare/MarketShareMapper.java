package back.tesis.mapper.marketshare;

import back.tesis.dto.marketshare.PriceGapDto;
import back.tesis.modules.marketshare.VentaMensualMarketShareRepository;

public final class MarketShareMapper {

    private MarketShareMapper() {
    }

    public static PriceGapDto toDto(VentaMensualMarketShareRepository.PriceGapRow row) {
        return new PriceGapDto(row.getProductoId(), row.getNombreProducto(), row.getPrecioUnitario(),
                row.getPriceGapPct(), row.getAnio(), row.getMes());
    }
}
