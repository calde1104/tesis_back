package back.tesis.mapper.sales;

import back.tesis.dto.sales.CategoryFilterOptionDto;
import back.tesis.dto.sales.FilterOptionDto;
import back.tesis.dto.sales.SaleItemDto;
import back.tesis.entities.Categoria;
import back.tesis.entities.Departamento;
import back.tesis.entities.Marca;
import back.tesis.modules.sales.SalesRepository;

public final class SalesMapper {

    private SalesMapper() {
    }

    public static SaleItemDto toDto(SalesRepository.SaleRow row) {
        return new SaleItemDto(
                row.getProductoId(), row.getAnio(), row.getMes(),
                row.getUnidadesVendidas(), row.getMontoFacturado(), row.getPrecioUnitario(),
                row.getSku(), row.getDescripcionProducto(), row.getMarcaNombre(),
                row.getCategoriaNombre(), row.getContenidoNeto(), row.getUnidadMedida()
        );
    }

    public static FilterOptionDto toDto(Marca entity) {
        return new FilterOptionDto(entity.getId(), entity.getNombre());
    }

    public static FilterOptionDto toDto(Departamento entity) {
        return new FilterOptionDto(String.valueOf(entity.getId()), entity.getNombre());
    }

    public static CategoryFilterOptionDto toCategoryFilterOptionDto(Categoria entity) {
        return new CategoryFilterOptionDto(
                String.valueOf(entity.getId()), entity.getNombre(), entity.getDepartamento().getId());
    }
}
