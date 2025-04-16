package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.CommentsAsserts.*;
import static com.mycompany.myapp.domain.CommentsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommentsMapperTest {

    private CommentsMapper commentsMapper;

    @BeforeEach
    void setUp() {
        commentsMapper = new CommentsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCommentsSample1();
        var actual = commentsMapper.toEntity(commentsMapper.toDto(expected));
        assertCommentsAllPropertiesEquals(expected, actual);
    }
}
