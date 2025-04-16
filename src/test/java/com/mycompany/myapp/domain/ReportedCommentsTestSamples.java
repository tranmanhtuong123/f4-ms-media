package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ReportedCommentsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static ReportedComments getReportedCommentsSample1() {
        return new ReportedComments().id(1L).reportedBy(1L).reason("reason1");
    }

    public static ReportedComments getReportedCommentsSample2() {
        return new ReportedComments().id(2L).reportedBy(2L).reason("reason2");
    }

    public static ReportedComments getReportedCommentsRandomSampleGenerator() {
        return new ReportedComments()
            .id(longCount.incrementAndGet())
            .reportedBy(longCount.incrementAndGet())
            .reason(UUID.randomUUID().toString());
    }
}
