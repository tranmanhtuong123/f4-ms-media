package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Reactions;
import com.mycompany.myapp.repository.ReactionsRepository;
import com.mycompany.myapp.service.criteria.ReactionsCriteria;
import com.mycompany.myapp.service.dto.ReactionsDTO;
import com.mycompany.myapp.service.mapper.ReactionsMapper;
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
 * Service for executing complex queries for {@link Reactions} entities in the database.
 * The main input is a {@link ReactionsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ReactionsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ReactionsQueryService extends QueryService<Reactions> {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionsQueryService.class);

    private final ReactionsRepository reactionsRepository;

    private final ReactionsMapper reactionsMapper;

    public ReactionsQueryService(ReactionsRepository reactionsRepository, ReactionsMapper reactionsMapper) {
        this.reactionsRepository = reactionsRepository;
        this.reactionsMapper = reactionsMapper;
    }

    /**
     * Return a {@link Page} of {@link ReactionsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ReactionsDTO> findByCriteria(ReactionsCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Reactions> specification = createSpecification(criteria);
        return reactionsRepository.findAll(specification, page).map(reactionsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ReactionsCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Reactions> specification = createSpecification(criteria);
        return reactionsRepository.count(specification);
    }

    /**
     * Function to convert {@link ReactionsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Reactions> createSpecification(ReactionsCriteria criteria) {
        Specification<Reactions> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Reactions_.id));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUserId(), Reactions_.userId));
            }
            if (criteria.getReactionType() != null) {
                specification = specification.and(buildSpecification(criteria.getReactionType(), Reactions_.reactionType));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Reactions_.createdAt));
            }
            if (criteria.getPostId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getPostId(), root -> root.join(Reactions_.post, JoinType.LEFT).get(Posts_.id))
                );
            }
            if (criteria.getCommentId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getCommentId(), root -> root.join(Reactions_.comment, JoinType.LEFT).get(Comments_.id))
                );
            }
        }
        return specification;
    }
}
