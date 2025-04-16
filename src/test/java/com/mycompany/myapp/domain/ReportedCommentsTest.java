package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.CommentsTestSamples.*;
import static com.mycompany.myapp.domain.ReportedCommentsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReportedCommentsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReportedComments.class);
        ReportedComments reportedComments1 = getReportedCommentsSample1();
        ReportedComments reportedComments2 = new ReportedComments();
        assertThat(reportedComments1).isNotEqualTo(reportedComments2);

        reportedComments2.setId(reportedComments1.getId());
        assertThat(reportedComments1).isEqualTo(reportedComments2);

        reportedComments2 = getReportedCommentsSample2();
        assertThat(reportedComments1).isNotEqualTo(reportedComments2);
    }

    @Test
    void commentTest() {
        ReportedComments reportedComments = getReportedCommentsRandomSampleGenerator();
        Comments commentsBack = getCommentsRandomSampleGenerator();

        reportedComments.setComment(commentsBack);
        assertThat(reportedComments.getComment()).isEqualTo(commentsBack);

        reportedComments.comment(null);
        assertThat(reportedComments.getComment()).isNull();
    }
}
