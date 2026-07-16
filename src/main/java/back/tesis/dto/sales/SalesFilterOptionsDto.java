package back.tesis.dto.sales;

import java.util.List;

public record SalesFilterOptionsDto(
        List<FilterOptionDto> marcas,
        List<FilterOptionDto> departamentos,
        List<FilterOptionDto> categorias
) {
}
