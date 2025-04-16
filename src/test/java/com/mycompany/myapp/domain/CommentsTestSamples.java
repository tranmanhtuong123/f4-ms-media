package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CommentsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Comments getCommentsSample1() {
        return new Comments().id(1L).userId(1L).content("content1");
    }

    public static Comments getCommentsSample2() {
        return new Comments().id(2L).userId(2L).content("content2");
    }

    public static Comments getCommentsRandomSampleGenerator() {
        return new Comments().id(longCount.incrementAndGet()).userId(longCount.incrementAndGet()).content(UUID.randomUUID().toString());
    }
}
