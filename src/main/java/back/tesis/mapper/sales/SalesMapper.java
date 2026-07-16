package back.tesis.mapper.sales;

import back.tesis.dto.sales.FilterOptionDto;
import back.tesis.dto.sales.SaleItemDto;
import back.tesis.entities.Categoria;
import back.tesis.entities.Departamento;
import back.tesis.entities.Marca;
import back.tesis.entities.VentaMensual;

public final class SalesMapper {

    private SalesMapper() {
    }

    public static SaleItemDto toDto(VentaMensual entity) {
        return new SaleItemDto(
                entity.getProductoId(), entity.getAnio(), entity.getMes(),
                entity.getUnidadesVendidas(), entity.getMontoFacturado(), entity.getPrecioUnitario());
    }

    public static FilterOptionDto toDto(Marca entity) {
        return new FilterOptionDto(entity.getId(), entity.getNombre());
    }

    public static FilterOptionDto toDto(Departamento entity) {
        return new FilterOptionDto(String.valueOf(entity.getId()), entity.getNombre());
    }

    public static FilterOptionDto toDto(Categoria entity) {
        return new FilterOptionDto(String.valueOf(entity.getId()), entity.getNombre());
    }
}
