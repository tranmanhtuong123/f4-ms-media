package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ReportedCommentsCriteriaTest {

    @Test
    void newReportedCommentsCriteriaHasAllFiltersNullTest() {
        var reportedCommentsCriteria = new ReportedCommentsCriteria();
        assertThat(reportedCommentsCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void reportedCommentsCriteriaFluentMethodsCreatesFiltersTest() {
        var reportedCommentsCriteria = new ReportedCommentsCriteria();

        setAllFilters(reportedCommentsCriteria);

        assertThat(reportedCommentsCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void reportedCommentsCriteriaCopyCreatesNullFilterTest() {
        var reportedCommentsCriteria = new ReportedCommentsCriteria();
        var copy = reportedCommentsCriteria.copy();

        assertThat(reportedCommentsCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(reportedCommentsCriteria)
        );
    }

    @Test
    void reportedCommentsCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var reportedCommentsCriteria = new ReportedCommentsCriteria();
        setAllFilters(reportedCommentsCriteria);

        var copy = reportedCommentsCriteria.copy();

        assertThat(reportedCommentsCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(reportedCommentsCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var reportedCommentsCriteria = new ReportedCommentsCriteria();

        assertThat(reportedCommentsCriteria).hasToString("ReportedCommentsCriteria{}");
    }

    private static void setAllFilters(ReportedCommentsCriteria reportedCommentsCriteria) {
        reportedCommentsCriteria.id();
        reportedCommentsCriteria.reportedBy();
        reportedCommentsCriteria.reason();
        reportedCommentsCriteria.createdAt();
        reportedCommentsCriteria.commentId();
        reportedCommentsCriteria.distinct();
    }

    private static Condition<ReportedCommentsCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getReportedBy()) &&
                condition.apply(criteria.getReason()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getCommentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ReportedCommentsCriteria> copyFiltersAre(
        ReportedCommentsCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getReportedBy(), copy.getReportedBy()) &&
                condition.apply(criteria.getReason(), copy.getReason()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getCommentId(), copy.getCommentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
