package com.vibeprivate.gui;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AdminGuiPageTest {
    @Test
    void clampsRequestedPageToAvailableRange() {
        AdminGuiPage negativePage = new AdminGuiPage(-3, 90);
        AdminGuiPage tooLargePage = new AdminGuiPage(5, 90);

        assertEquals(0, negativePage.page());
        assertEquals(1, tooLargePage.page());
    }

    @Test
    void slicesContentByGuiPageSize() {
        List<Integer> items = IntStream.range(0, 91).boxed().toList();

        assertEquals(items.subList(0, 45), new AdminGuiPage(0, items.size()).slice(items));
        assertEquals(items.subList(45, 90), new AdminGuiPage(1, items.size()).slice(items));
        assertEquals(List.of(90), new AdminGuiPage(2, items.size()).slice(items));
    }

    @Test
    void reportsNavigationAvailability() {
        AdminGuiPage first = new AdminGuiPage(0, 46);
        AdminGuiPage last = new AdminGuiPage(1, 46);

        assertFalse(first.hasPrevious());
        assertTrue(first.hasNext());
        assertTrue(last.hasPrevious());
        assertFalse(last.hasNext());
    }
}
