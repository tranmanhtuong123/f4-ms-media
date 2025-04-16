package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Comments;
import com.mycompany.myapp.repository.CommentsRepository;
import com.mycompany.myapp.service.criteria.CommentsCriteria;
import com.mycompany.myapp.service.dto.CommentsDTO;
import com.mycompany.myapp.service.mapper.CommentsMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Comments} entities in the database.
 * The main input is a {@link CommentsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link CommentsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CommentsQueryService extends QueryService<Comments> {

    private static final Logger LOG = LoggerFactory.getLogger(CommentsQueryService.class);

    private final CommentsRepository commentsRepository;

    private final CommentsMapper commentsMapper;

    public CommentsQueryService(CommentsRepository commentsRepository, CommentsMapper commentsMapper) {
        this.commentsRepository = commentsRepository;
        this.commentsMapper = commentsMapper;
    }

    /**
     * Return a {@link Page} of {@link CommentsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CommentsDTO> findByCriteria(CommentsCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Comments> specification = createSpecification(criteria);
        return commentsRepository.findAll(specification, page).map(commentsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CommentsCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Comments> specification = createSpecification(criteria);
        return commentsRepository.count(specification);
    }

    /**
     * Function to convert {@link CommentsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Comments> createSpecification(CommentsCriteria criteria) {
        Specification<Comments> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Comments_.id));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUserId(), Comments_.userId));
            }
            if (criteria.getContent() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContent(), Comments_.content));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Comments_.createdAt));
            }
            if (criteria.getPostId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getPostId(), root -> root.join(Comments_.post, JoinType.LEFT).get(Posts_.id))
                );
            }
            if (criteria.getParentCommentId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getParentCommentId(), root ->
                        root.join(Comments_.parentComment, JoinType.LEFT).get(Comments_.id)
                    )
                );
            }
        }
        return specification;
    }
}
