package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.ReportedCommentsAsserts.*;
import static com.mycompany.myapp.domain.ReportedCommentsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportedCommentsMapperTest {

    private ReportedCommentsMapper reportedCommentsMapper;

    @BeforeEach
    void setUp() {
        reportedCommentsMapper = new ReportedCommentsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getReportedCommentsSample1();
        var actual = reportedCommentsMapper.toEntity(reportedCommentsMapper.toDto(expected));
        assertReportedCommentsAllPropertiesEquals(expected, actual);
    }
}
