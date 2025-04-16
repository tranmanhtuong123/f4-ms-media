package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PostsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PostsDTO.class);
        PostsDTO postsDTO1 = new PostsDTO();
        postsDTO1.setId(1L);
        PostsDTO postsDTO2 = new PostsDTO();
        assertThat(postsDTO1).isNotEqualTo(postsDTO2);
        postsDTO2.setId(postsDTO1.getId());
        assertThat(postsDTO1).isEqualTo(postsDTO2);
        postsDTO2.setId(2L);
        assertThat(postsDTO1).isNotEqualTo(postsDTO2);
        postsDTO1.setId(null);
        assertThat(postsDTO1).isNotEqualTo(postsDTO2);
    }
}
