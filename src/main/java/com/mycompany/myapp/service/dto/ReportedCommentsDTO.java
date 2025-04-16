package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.ReportedComments} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReportedCommentsDTO implements Serializable {

    private Long id;

    private Long reportedBy;

    private String reason;

    private Instant createdAt;

    private CommentsDTO comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Long reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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
        if (!(o instanceof ReportedCommentsDTO)) {
            return false;
        }

        ReportedCommentsDTO reportedCommentsDTO = (ReportedCommentsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, reportedCommentsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReportedCommentsDTO{" +
            "id=" + getId() +
            ", reportedBy=" + getReportedBy() +
            ", reason='" + getReason() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", comment=" + getComment() +
            "}";
    }
}
