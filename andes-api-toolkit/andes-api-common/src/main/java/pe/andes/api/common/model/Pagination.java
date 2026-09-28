package pe.andes.api.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Page-level metadata used by {@link PageResponse}.
 */
public final class Pagination implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public Pagination(int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public static Pagination of(int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean isFirst = page == 0;
        boolean isLast = page >= totalPages - 1;
        return new Pagination(page, size, totalElements, totalPages, isFirst, isLast);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pagination that)) {
            return false;
        }
        return page == that.page
                && size == that.size
                && totalElements == that.totalElements
                && totalPages == that.totalPages
                && first == that.first
                && last == that.last;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, totalElements, totalPages, first, last);
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", first=" + first +
                ", last=" + last +
                '}';
    }
}
