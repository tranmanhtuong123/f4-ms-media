package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReactionsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReactionsDTO.class);
        ReactionsDTO reactionsDTO1 = new ReactionsDTO();
        reactionsDTO1.setId(1L);
        ReactionsDTO reactionsDTO2 = new ReactionsDTO();
        assertThat(reactionsDTO1).isNotEqualTo(reactionsDTO2);
        reactionsDTO2.setId(reactionsDTO1.getId());
        assertThat(reactionsDTO1).isEqualTo(reactionsDTO2);
        reactionsDTO2.setId(2L);
        assertThat(reactionsDTO1).isNotEqualTo(reactionsDTO2);
        reactionsDTO1.setId(null);
        assertThat(reactionsDTO1).isNotEqualTo(reactionsDTO2);
    }
}
