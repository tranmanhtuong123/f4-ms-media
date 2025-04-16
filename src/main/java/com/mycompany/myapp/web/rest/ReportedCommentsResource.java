package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.ReportedCommentsRepository;
import com.mycompany.myapp.service.ReportedCommentsQueryService;
import com.mycompany.myapp.service.ReportedCommentsService;
import com.mycompany.myapp.service.criteria.ReportedCommentsCriteria;
import com.mycompany.myapp.service.dto.ReportedCommentsDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.ReportedComments}.
 */
@RestController
@RequestMapping("/api/reported-comments")
public class ReportedCommentsResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReportedCommentsResource.class);

    private static final String ENTITY_NAME = "msMediaReportedComments";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReportedCommentsService reportedCommentsService;

    private final ReportedCommentsRepository reportedCommentsRepository;

    private final ReportedCommentsQueryService reportedCommentsQueryService;

    public ReportedCommentsResource(
        ReportedCommentsService reportedCommentsService,
        ReportedCommentsRepository reportedCommentsRepository,
        ReportedCommentsQueryService reportedCommentsQueryService
    ) {
        this.reportedCommentsService = reportedCommentsService;
        this.reportedCommentsRepository = reportedCommentsRepository;
        this.reportedCommentsQueryService = reportedCommentsQueryService;
    }

    /**
     * {@code POST  /reported-comments} : Create a new reportedComments.
     *
     * @param reportedCommentsDTO the reportedCommentsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reportedCommentsDTO, or with status {@code 400 (Bad Request)} if the reportedComments has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ReportedCommentsDTO> createReportedComments(@RequestBody ReportedCommentsDTO reportedCommentsDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ReportedComments : {}", reportedCommentsDTO);
        if (reportedCommentsDTO.getId() != null) {
            throw new BadRequestAlertException("A new reportedComments cannot already have an ID", ENTITY_NAME, "idexists");
        }
        reportedCommentsDTO = reportedCommentsService.save(reportedCommentsDTO);
        return ResponseEntity.created(new URI("/api/reported-comments/" + reportedCommentsDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, reportedCommentsDTO.getId().toString()))
            .body(reportedCommentsDTO);
    }

    /**
     * {@code PUT  /reported-comments/:id} : Updates an existing reportedComments.
     *
     * @param id the id of the reportedCommentsDTO to save.
     * @param reportedCommentsDTO the reportedCommentsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reportedCommentsDTO,
     * or with status {@code 400 (Bad Request)} if the reportedCommentsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reportedCommentsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportedCommentsDTO> updateReportedComments(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ReportedCommentsDTO reportedCommentsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ReportedComments : {}, {}", id, reportedCommentsDTO);
        if (reportedCommentsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reportedCommentsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reportedCommentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        reportedCommentsDTO = reportedCommentsService.update(reportedCommentsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reportedCommentsDTO.getId().toString()))
            .body(reportedCommentsDTO);
    }

    /**
     * {@code PATCH  /reported-comments/:id} : Partial updates given fields of an existing reportedComments, field will ignore if it is null
     *
     * @param id the id of the reportedCommentsDTO to save.
     * @param reportedCommentsDTO the reportedCommentsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reportedCommentsDTO,
     * or with status {@code 400 (Bad Request)} if the reportedCommentsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reportedCommentsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reportedCommentsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReportedCommentsDTO> partialUpdateReportedComments(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ReportedCommentsDTO reportedCommentsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ReportedComments partially : {}, {}", id, reportedCommentsDTO);
        if (reportedCommentsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reportedCommentsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reportedCommentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReportedCommentsDTO> result = reportedCommentsService.partialUpdate(reportedCommentsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reportedCommentsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /reported-comments} : get all the reportedComments.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reportedComments in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ReportedCommentsDTO>> getAllReportedComments(
        ReportedCommentsCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get ReportedComments by criteria: {}", criteria);

        Page<ReportedCommentsDTO> page = reportedCommentsQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reported-comments/count} : count all the reportedComments.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countReportedComments(ReportedCommentsCriteria criteria) {
        LOG.debug("REST request to count ReportedComments by criteria: {}", criteria);
        return ResponseEntity.ok().body(reportedCommentsQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /reported-comments/:id} : get the "id" reportedComments.
     *
     * @param id the id of the reportedCommentsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reportedCommentsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportedCommentsDTO> getReportedComments(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ReportedComments : {}", id);
        Optional<ReportedCommentsDTO> reportedCommentsDTO = reportedCommentsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reportedCommentsDTO);
    }

    /**
     * {@code DELETE  /reported-comments/:id} : delete the "id" reportedComments.
     *
     * @param id the id of the reportedCommentsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReportedComments(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ReportedComments : {}", id);
        reportedCommentsService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
