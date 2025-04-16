package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReportedCommentsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReportedCommentsDTO.class);
        ReportedCommentsDTO reportedCommentsDTO1 = new ReportedCommentsDTO();
        reportedCommentsDTO1.setId(1L);
        ReportedCommentsDTO reportedCommentsDTO2 = new ReportedCommentsDTO();
        assertThat(reportedCommentsDTO1).isNotEqualTo(reportedCommentsDTO2);
        reportedCommentsDTO2.setId(reportedCommentsDTO1.getId());
        assertThat(reportedCommentsDTO1).isEqualTo(reportedCommentsDTO2);
        reportedCommentsDTO2.setId(2L);
        assertThat(reportedCommentsDTO1).isNotEqualTo(reportedCommentsDTO2);
        reportedCommentsDTO1.setId(null);
        assertThat(reportedCommentsDTO1).isNotEqualTo(reportedCommentsDTO2);
    }
}
