package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.Posts;
import com.mycompany.myapp.repository.PostsRepository;
import com.mycompany.myapp.service.criteria.PostsCriteria;
import com.mycompany.myapp.service.dto.PostsDTO;
import com.mycompany.myapp.service.mapper.PostsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Posts} entities in the database.
 * The main input is a {@link PostsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link PostsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PostsQueryService extends QueryService<Posts> {

    private static final Logger LOG = LoggerFactory.getLogger(PostsQueryService.class);

    private final PostsRepository postsRepository;

    private final PostsMapper postsMapper;

    public PostsQueryService(PostsRepository postsRepository, PostsMapper postsMapper) {
        this.postsRepository = postsRepository;
        this.postsMapper = postsMapper;
    }

    /**
     * Return a {@link Page} of {@link PostsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<PostsDTO> findByCriteria(PostsCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Posts> specification = createSpecification(criteria);
        return postsRepository.findAll(specification, page).map(postsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PostsCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Posts> specification = createSpecification(criteria);
        return postsRepository.count(specification);
    }

    /**
     * Function to convert {@link PostsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Posts> createSpecification(PostsCriteria criteria) {
        Specification<Posts> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Posts_.id));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUserId(), Posts_.userId));
            }
            if (criteria.getContent() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContent(), Posts_.content));
            }
            if (criteria.getMediaUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMediaUrl(), Posts_.mediaUrl));
            }
            if (criteria.getIsPrivate() != null) {
                specification = specification.and(buildSpecification(criteria.getIsPrivate(), Posts_.isPrivate));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Posts_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Posts_.updatedAt));
            }
        }
        return specification;
    }
}
