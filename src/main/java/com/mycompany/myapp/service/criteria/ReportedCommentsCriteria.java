package com.mycompany.myapp.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.ReportedComments} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.ReportedCommentsResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /reported-comments?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReportedCommentsCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter reportedBy;

    private StringFilter reason;

    private InstantFilter createdAt;

    private LongFilter commentId;

    private Boolean distinct;

    public ReportedCommentsCriteria() {}

    public ReportedCommentsCriteria(ReportedCommentsCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.reportedBy = other.optionalReportedBy().map(LongFilter::copy).orElse(null);
        this.reason = other.optionalReason().map(StringFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.commentId = other.optionalCommentId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ReportedCommentsCriteria copy() {
        return new ReportedCommentsCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LongFilter getReportedBy() {
        return reportedBy;
    }

    public Optional<LongFilter> optionalReportedBy() {
        return Optional.ofNullable(reportedBy);
    }

    public LongFilter reportedBy() {
        if (reportedBy == null) {
            setReportedBy(new LongFilter());
        }
        return reportedBy;
    }

    public void setReportedBy(LongFilter reportedBy) {
        this.reportedBy = reportedBy;
    }

    public StringFilter getReason() {
        return reason;
    }

    public Optional<StringFilter> optionalReason() {
        return Optional.ofNullable(reason);
    }

    public StringFilter reason() {
        if (reason == null) {
            setReason(new StringFilter());
        }
        return reason;
    }

    public void setReason(StringFilter reason) {
        this.reason = reason;
    }

    public InstantFilter getCreatedAt() {
        return createdAt;
    }

    public Optional<InstantFilter> optionalCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public InstantFilter createdAt() {
        if (createdAt == null) {
            setCreatedAt(new InstantFilter());
        }
        return createdAt;
    }

    public void setCreatedAt(InstantFilter createdAt) {
        this.createdAt = createdAt;
    }

    public LongFilter getCommentId() {
        return commentId;
    }

    public Optional<LongFilter> optionalCommentId() {
        return Optional.ofNullable(commentId);
    }

    public LongFilter commentId() {
        if (commentId == null) {
            setCommentId(new LongFilter());
        }
        return commentId;
    }

    public void setCommentId(LongFilter commentId) {
        this.commentId = commentId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReportedCommentsCriteria that = (ReportedCommentsCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(reportedBy, that.reportedBy) &&
            Objects.equals(reason, that.reason) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(commentId, that.commentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportedBy, reason, createdAt, commentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReportedCommentsCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalReportedBy().map(f -> "reportedBy=" + f + ", ").orElse("") +
            optionalReason().map(f -> "reason=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalCommentId().map(f -> "commentId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
