package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ReactionsCriteriaTest {

    @Test
    void newReactionsCriteriaHasAllFiltersNullTest() {
        var reactionsCriteria = new ReactionsCriteria();
        assertThat(reactionsCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void reactionsCriteriaFluentMethodsCreatesFiltersTest() {
        var reactionsCriteria = new ReactionsCriteria();

        setAllFilters(reactionsCriteria);

        assertThat(reactionsCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void reactionsCriteriaCopyCreatesNullFilterTest() {
        var reactionsCriteria = new ReactionsCriteria();
        var copy = reactionsCriteria.copy();

        assertThat(reactionsCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(reactionsCriteria)
        );
    }

    @Test
    void reactionsCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var reactionsCriteria = new ReactionsCriteria();
        setAllFilters(reactionsCriteria);

        var copy = reactionsCriteria.copy();

        assertThat(reactionsCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(reactionsCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var reactionsCriteria = new ReactionsCriteria();

        assertThat(reactionsCriteria).hasToString("ReactionsCriteria{}");
    }

    private static void setAllFilters(ReactionsCriteria reactionsCriteria) {
        reactionsCriteria.id();
        reactionsCriteria.userId();
        reactionsCriteria.reactionType();
        reactionsCriteria.createdAt();
        reactionsCriteria.postId();
        reactionsCriteria.commentId();
        reactionsCriteria.distinct();
    }

    private static Condition<ReactionsCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getReactionType()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getPostId()) &&
                condition.apply(criteria.getCommentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ReactionsCriteria> copyFiltersAre(ReactionsCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getReactionType(), copy.getReactionType()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getPostId(), copy.getPostId()) &&
                condition.apply(criteria.getCommentId(), copy.getCommentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
