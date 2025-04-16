package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.CommentsTestSamples.*;
import static com.mycompany.myapp.domain.CommentsTestSamples.*;
import static com.mycompany.myapp.domain.PostsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CommentsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Comments.class);
        Comments comments1 = getCommentsSample1();
        Comments comments2 = new Comments();
        assertThat(comments1).isNotEqualTo(comments2);

        comments2.setId(comments1.getId());
        assertThat(comments1).isEqualTo(comments2);

        comments2 = getCommentsSample2();
        assertThat(comments1).isNotEqualTo(comments2);
    }

    @Test
    void postTest() {
        Comments comments = getCommentsRandomSampleGenerator();
        Posts postsBack = getPostsRandomSampleGenerator();

        comments.setPost(postsBack);
        assertThat(comments.getPost()).isEqualTo(postsBack);

        comments.post(null);
        assertThat(comments.getPost()).isNull();
    }

    @Test
    void parentCommentTest() {
        Comments comments = getCommentsRandomSampleGenerator();
        Comments commentsBack = getCommentsRandomSampleGenerator();

        comments.setParentComment(commentsBack);
        assertThat(comments.getParentComment()).isEqualTo(commentsBack);

        comments.parentComment(null);
        assertThat(comments.getParentComment()).isNull();
    }
}
