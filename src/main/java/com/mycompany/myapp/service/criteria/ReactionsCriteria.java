package com.mycompany.myapp.service.criteria;

import com.mycompany.myapp.domain.enumeration.ReactionType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.Reactions} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.ReactionsResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /reactions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReactionsCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ReactionType
     */
    public static class ReactionTypeFilter extends Filter<ReactionType> {

        public ReactionTypeFilter() {}

        public ReactionTypeFilter(ReactionTypeFilter filter) {
            super(filter);
        }

        @Override
        public ReactionTypeFilter copy() {
            return new ReactionTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter userId;

    private ReactionTypeFilter reactionType;

    private InstantFilter createdAt;

    private LongFilter postId;

    private LongFilter commentId;

    private Boolean distinct;

    public ReactionsCriteria() {}

    public ReactionsCriteria(ReactionsCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.reactionType = other.optionalReactionType().map(ReactionTypeFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.postId = other.optionalPostId().map(LongFilter::copy).orElse(null);
        this.commentId = other.optionalCommentId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ReactionsCriteria copy() {
        return new ReactionsCriteria(this);
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

    public LongFilter getUserId() {
        return userId;
    }

    public Optional<LongFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public LongFilter userId() {
        if (userId == null) {
            setUserId(new LongFilter());
        }
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }

    public ReactionTypeFilter getReactionType() {
        return reactionType;
    }

    public Optional<ReactionTypeFilter> optionalReactionType() {
        return Optional.ofNullable(reactionType);
    }

    public ReactionTypeFilter reactionType() {
        if (reactionType == null) {
            setReactionType(new ReactionTypeFilter());
        }
        return reactionType;
    }

    public void setReactionType(ReactionTypeFilter reactionType) {
        this.reactionType = reactionType;
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

    public LongFilter getPostId() {
        return postId;
    }

    public Optional<LongFilter> optionalPostId() {
        return Optional.ofNullable(postId);
    }

    public LongFilter postId() {
        if (postId == null) {
            setPostId(new LongFilter());
        }
        return postId;
    }

    public void setPostId(LongFilter postId) {
        this.postId = postId;
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
        final ReactionsCriteria that = (ReactionsCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(reactionType, that.reactionType) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(postId, that.postId) &&
            Objects.equals(commentId, that.commentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, reactionType, createdAt, postId, commentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReactionsCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalReactionType().map(f -> "reactionType=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalPostId().map(f -> "postId=" + f + ", ").orElse("") +
            optionalCommentId().map(f -> "commentId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
