package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class PostsCriteriaTest {

    @Test
    void newPostsCriteriaHasAllFiltersNullTest() {
        var postsCriteria = new PostsCriteria();
        assertThat(postsCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void postsCriteriaFluentMethodsCreatesFiltersTest() {
        var postsCriteria = new PostsCriteria();

        setAllFilters(postsCriteria);

        assertThat(postsCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void postsCriteriaCopyCreatesNullFilterTest() {
        var postsCriteria = new PostsCriteria();
        var copy = postsCriteria.copy();

        assertThat(postsCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(postsCriteria)
        );
    }

    @Test
    void postsCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var postsCriteria = new PostsCriteria();
        setAllFilters(postsCriteria);

        var copy = postsCriteria.copy();

        assertThat(postsCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(postsCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var postsCriteria = new PostsCriteria();

        assertThat(postsCriteria).hasToString("PostsCriteria{}");
    }

    private static void setAllFilters(PostsCriteria postsCriteria) {
        postsCriteria.id();
        postsCriteria.userId();
        postsCriteria.content();
        postsCriteria.mediaUrl();
        postsCriteria.isPrivate();
        postsCriteria.createdAt();
        postsCriteria.updatedAt();
        postsCriteria.distinct();
    }

    private static Condition<PostsCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getContent()) &&
                condition.apply(criteria.getMediaUrl()) &&
                condition.apply(criteria.getIsPrivate()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<PostsCriteria> copyFiltersAre(PostsCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getContent(), copy.getContent()) &&
                condition.apply(criteria.getMediaUrl(), copy.getMediaUrl()) &&
                condition.apply(criteria.getIsPrivate(), copy.getIsPrivate()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
