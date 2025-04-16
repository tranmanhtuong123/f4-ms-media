package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.ReactionsAsserts.*;
import static com.mycompany.myapp.domain.ReactionsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReactionsMapperTest {

    private ReactionsMapper reactionsMapper;

    @BeforeEach
    void setUp() {
        reactionsMapper = new ReactionsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getReactionsSample1();
        var actual = reactionsMapper.toEntity(reactionsMapper.toDto(expected));
        assertReactionsAllPropertiesEquals(expected, actual);
    }
}
