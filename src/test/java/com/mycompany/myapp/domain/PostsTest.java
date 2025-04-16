package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.PostsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PostsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Posts.class);
        Posts posts1 = getPostsSample1();
        Posts posts2 = new Posts();
        assertThat(posts1).isNotEqualTo(posts2);

        posts2.setId(posts1.getId());
        assertThat(posts1).isEqualTo(posts2);

        posts2 = getPostsSample2();
        assertThat(posts1).isNotEqualTo(posts2);
    }
}
