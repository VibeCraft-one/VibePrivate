package com.vibeprivate.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VibeRegionGuardApiTest {

    @Test
    void facadeRejectsNullApi() {
        assertThrows(NullPointerException.class, () -> new VibeRegionGuardApi(null));
    }
}
