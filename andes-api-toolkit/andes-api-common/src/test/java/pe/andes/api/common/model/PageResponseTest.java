package pe.andes.api.common.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void computesPaginationCorrectly() {
        PageResponse<String> page = PageResponse.of(List.of("a", "b"), 0, 2, 5);
        assertEquals(3, page.getPagination().getTotalPages());
        assertTrue(page.getPagination().isFirst());
        assertFalse(page.getPagination().isLast());
    }

    @Test
    void lastPageDetection() {
        Pagination pagination = Pagination.of(2, 2, 5);
        assertTrue(pagination.isLast());
    }
}
