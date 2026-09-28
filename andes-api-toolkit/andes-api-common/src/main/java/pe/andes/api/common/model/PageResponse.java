package pe.andes.api.common.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a paginated collection of items, meant to be carried inside
 * {@link ApiResponse#getData()}.
 *
 * @param <T> the type of each element in the page
 */
public final class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<T> content;
    private final Pagination pagination;

    public PageResponse(List<T> content, Pagination pagination) {
        this.content = content != null ? List.copyOf(content) : Collections.emptyList();
        this.pagination = pagination;
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        return new PageResponse<>(content, Pagination.of(page, size, totalElements));
    }

    public List<T> getContent() {
        return content;
    }

    public Pagination getPagination() {
        return pagination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PageResponse<?> that)) {
            return false;
        }
        return Objects.equals(content, that.content) && Objects.equals(pagination, that.pagination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, pagination);
    }

    @Override
    public String toString() {
        return "PageResponse{" +
                "content=" + content +
                ", pagination=" + pagination +
                '}';
    }
}
