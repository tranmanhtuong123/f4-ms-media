package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CommentsCriteriaTest {

    @Test
    void newCommentsCriteriaHasAllFiltersNullTest() {
        var commentsCriteria = new CommentsCriteria();
        assertThat(commentsCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void commentsCriteriaFluentMethodsCreatesFiltersTest() {
        var commentsCriteria = new CommentsCriteria();

        setAllFilters(commentsCriteria);

        assertThat(commentsCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void commentsCriteriaCopyCreatesNullFilterTest() {
        var commentsCriteria = new CommentsCriteria();
        var copy = commentsCriteria.copy();

        assertThat(commentsCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(commentsCriteria)
        );
    }

    @Test
    void commentsCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var commentsCriteria = new CommentsCriteria();
        setAllFilters(commentsCriteria);

        var copy = commentsCriteria.copy();

        assertThat(commentsCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(commentsCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var commentsCriteria = new CommentsCriteria();

        assertThat(commentsCriteria).hasToString("CommentsCriteria{}");
    }

    private static void setAllFilters(CommentsCriteria commentsCriteria) {
        commentsCriteria.id();
        commentsCriteria.userId();
        commentsCriteria.content();
        commentsCriteria.createdAt();
        commentsCriteria.postId();
        commentsCriteria.parentCommentId();
        commentsCriteria.distinct();
    }

    private static Condition<CommentsCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getContent()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getPostId()) &&
                condition.apply(criteria.getParentCommentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CommentsCriteria> copyFiltersAre(CommentsCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getContent(), copy.getContent()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getPostId(), copy.getPostId()) &&
                condition.apply(criteria.getParentCommentId(), copy.getParentCommentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
