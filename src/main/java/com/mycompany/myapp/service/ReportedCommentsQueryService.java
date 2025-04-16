package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.ReportedComments;
import com.mycompany.myapp.repository.ReportedCommentsRepository;
import com.mycompany.myapp.service.criteria.ReportedCommentsCriteria;
import com.mycompany.myapp.service.dto.ReportedCommentsDTO;
import com.mycompany.myapp.service.mapper.ReportedCommentsMapper;
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
 * Service for executing complex queries for {@link ReportedComments} entities in the database.
 * The main input is a {@link ReportedCommentsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ReportedCommentsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ReportedCommentsQueryService extends QueryService<ReportedComments> {

    private static final Logger LOG = LoggerFactory.getLogger(ReportedCommentsQueryService.class);

    private final ReportedCommentsRepository reportedCommentsRepository;

    private final ReportedCommentsMapper reportedCommentsMapper;

    public ReportedCommentsQueryService(
        ReportedCommentsRepository reportedCommentsRepository,
        ReportedCommentsMapper reportedCommentsMapper
    ) {
        this.reportedCommentsRepository = reportedCommentsRepository;
        this.reportedCommentsMapper = reportedCommentsMapper;
    }

    /**
     * Return a {@link Page} of {@link ReportedCommentsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ReportedCommentsDTO> findByCriteria(ReportedCommentsCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ReportedComments> specification = createSpecification(criteria);
        return reportedCommentsRepository.findAll(specification, page).map(reportedCommentsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ReportedCommentsCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<ReportedComments> specification = createSpecification(criteria);
        return reportedCommentsRepository.count(specification);
    }

    /**
     * Function to convert {@link ReportedCommentsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ReportedComments> createSpecification(ReportedCommentsCriteria criteria) {
        Specification<ReportedComments> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ReportedComments_.id));
            }
            if (criteria.getReportedBy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getReportedBy(), ReportedComments_.reportedBy));
            }
            if (criteria.getReason() != null) {
                specification = specification.and(buildStringSpecification(criteria.getReason(), ReportedComments_.reason));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), ReportedComments_.createdAt));
            }
            if (criteria.getCommentId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getCommentId(), root ->
                        root.join(ReportedComments_.comment, JoinType.LEFT).get(Comments_.id)
                    )
                );
            }
        }
        return specification;
    }
}
