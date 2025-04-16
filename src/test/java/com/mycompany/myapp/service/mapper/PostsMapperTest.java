package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.PostsAsserts.*;
import static com.mycompany.myapp.domain.PostsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostsMapperTest {

    private PostsMapper postsMapper;

    @BeforeEach
    void setUp() {
        postsMapper = new PostsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPostsSample1();
        var actual = postsMapper.toEntity(postsMapper.toDto(expected));
        assertPostsAllPropertiesEquals(expected, actual);
    }
}
