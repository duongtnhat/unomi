package com.unomi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UnomiApplicationTests {

    @Test
    void applicationCanBeConstructed() {
        assertThat(new UnomiApplication()).isNotNull();
    }
}
