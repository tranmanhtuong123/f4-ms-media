package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PostsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Posts getPostsSample1() {
        return new Posts().id(1L).userId(1L).content("content1").mediaUrl("mediaUrl1");
    }

    public static Posts getPostsSample2() {
        return new Posts().id(2L).userId(2L).content("content2").mediaUrl("mediaUrl2");
    }

    public static Posts getPostsRandomSampleGenerator() {
        return new Posts()
            .id(longCount.incrementAndGet())
            .userId(longCount.incrementAndGet())
            .content(UUID.randomUUID().toString())
            .mediaUrl(UUID.randomUUID().toString());
    }
}
