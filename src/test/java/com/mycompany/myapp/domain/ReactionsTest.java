package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.CommentsTestSamples.*;
import static com.mycompany.myapp.domain.PostsTestSamples.*;
import static com.mycompany.myapp.domain.ReactionsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReactionsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Reactions.class);
        Reactions reactions1 = getReactionsSample1();
        Reactions reactions2 = new Reactions();
        assertThat(reactions1).isNotEqualTo(reactions2);

        reactions2.setId(reactions1.getId());
        assertThat(reactions1).isEqualTo(reactions2);

        reactions2 = getReactionsSample2();
        assertThat(reactions1).isNotEqualTo(reactions2);
    }

    @Test
    void postTest() {
        Reactions reactions = getReactionsRandomSampleGenerator();
        Posts postsBack = getPostsRandomSampleGenerator();

        reactions.setPost(postsBack);
        assertThat(reactions.getPost()).isEqualTo(postsBack);

        reactions.post(null);
        assertThat(reactions.getPost()).isNull();
    }

    @Test
    void commentTest() {
        Reactions reactions = getReactionsRandomSampleGenerator();
        Comments commentsBack = getCommentsRandomSampleGenerator();

        reactions.setComment(commentsBack);
        assertThat(reactions.getComment()).isEqualTo(commentsBack);

        reactions.comment(null);
        assertThat(reactions.getComment()).isNull();
    }
}
