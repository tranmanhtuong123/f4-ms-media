package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.ReportedComments;
import com.mycompany.myapp.repository.ReportedCommentsRepository;
import com.mycompany.myapp.service.dto.ReportedCommentsDTO;
import com.mycompany.myapp.service.mapper.ReportedCommentsMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.ReportedComments}.
 */
@Service
@Transactional
public class ReportedCommentsService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportedCommentsService.class);

    private final ReportedCommentsRepository reportedCommentsRepository;

    private final ReportedCommentsMapper reportedCommentsMapper;

    public ReportedCommentsService(ReportedCommentsRepository reportedCommentsRepository, ReportedCommentsMapper reportedCommentsMapper) {
        this.reportedCommentsRepository = reportedCommentsRepository;
        this.reportedCommentsMapper = reportedCommentsMapper;
    }

    /**
     * Save a reportedComments.
     *
     * @param reportedCommentsDTO the entity to save.
     * @return the persisted entity.
     */
    public ReportedCommentsDTO save(ReportedCommentsDTO reportedCommentsDTO) {
        LOG.debug("Request to save ReportedComments : {}", reportedCommentsDTO);
        ReportedComments reportedComments = reportedCommentsMapper.toEntity(reportedCommentsDTO);
        reportedComments = reportedCommentsRepository.save(reportedComments);
        return reportedCommentsMapper.toDto(reportedComments);
    }

    /**
     * Update a reportedComments.
     *
     * @param reportedCommentsDTO the entity to save.
     * @return the persisted entity.
     */
    public ReportedCommentsDTO update(ReportedCommentsDTO reportedCommentsDTO) {
        LOG.debug("Request to update ReportedComments : {}", reportedCommentsDTO);
        ReportedComments reportedComments = reportedCommentsMapper.toEntity(reportedCommentsDTO);
        reportedComments = reportedCommentsRepository.save(reportedComments);
        return reportedCommentsMapper.toDto(reportedComments);
    }

    /**
     * Partially update a reportedComments.
     *
     * @param reportedCommentsDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReportedCommentsDTO> partialUpdate(ReportedCommentsDTO reportedCommentsDTO) {
        LOG.debug("Request to partially update ReportedComments : {}", reportedCommentsDTO);

        return reportedCommentsRepository
            .findById(reportedCommentsDTO.getId())
            .map(existingReportedComments -> {
                reportedCommentsMapper.partialUpdate(existingReportedComments, reportedCommentsDTO);

                return existingReportedComments;
            })
            .map(reportedCommentsRepository::save)
            .map(reportedCommentsMapper::toDto);
    }

    /**
     * Get one reportedComments by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReportedCommentsDTO> findOne(Long id) {
        LOG.debug("Request to get ReportedComments : {}", id);
        return reportedCommentsRepository.findById(id).map(reportedCommentsMapper::toDto);
    }

    /**
     * Delete the reportedComments by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ReportedComments : {}", id);
        reportedCommentsRepository.deleteById(id);
    }
}
