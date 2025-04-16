package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.ReactionType;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Reactions} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReactionsDTO implements Serializable {

    private Long id;

    private Long userId;

    private ReactionType reactionType;

    private Instant createdAt;

    private PostsDTO post;

    private CommentsDTO comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public PostsDTO getPost() {
        return post;
    }

    public void setPost(PostsDTO post) {
        this.post = post;
    }

    public CommentsDTO getComment() {
        return comment;
    }

    public void setComment(CommentsDTO comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReactionsDTO)) {
            return false;
        }

        ReactionsDTO reactionsDTO = (ReactionsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, reactionsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReactionsDTO{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", reactionType='" + getReactionType() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", post=" + getPost() +
            ", comment=" + getComment() +
            "}";
    }
}
