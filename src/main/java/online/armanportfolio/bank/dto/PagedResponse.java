package online.armanportfolio.bank.dto;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
        List<T> content, int page, int size,
        long totalElements, int totalPages, boolean first, boolean last
) {
    public static <E, T> PagedResponse<T> of(Page<E> p, Function<E, T> mapper) {
        return new PagedResponse<>(
                p.getContent().stream().map(mapper).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements(),
                p.getTotalPages(), p.isFirst(), p.isLast());
    }
}
