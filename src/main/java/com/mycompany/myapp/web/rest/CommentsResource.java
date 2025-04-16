package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.CommentsRepository;
import com.mycompany.myapp.service.CommentsQueryService;
import com.mycompany.myapp.service.CommentsService;
import com.mycompany.myapp.service.criteria.CommentsCriteria;
import com.mycompany.myapp.service.dto.CommentsDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.Comments}.
 */
@RestController
@RequestMapping("/api/comments")
public class CommentsResource {

    private static final Logger LOG = LoggerFactory.getLogger(CommentsResource.class);

    private static final String ENTITY_NAME = "msMediaComments";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CommentsService commentsService;

    private final CommentsRepository commentsRepository;

    private final CommentsQueryService commentsQueryService;

    public CommentsResource(
        CommentsService commentsService,
        CommentsRepository commentsRepository,
        CommentsQueryService commentsQueryService
    ) {
        this.commentsService = commentsService;
        this.commentsRepository = commentsRepository;
        this.commentsQueryService = commentsQueryService;
    }

    /**
     * {@code POST  /comments} : Create a new comments.
     *
     * @param commentsDTO the commentsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new commentsDTO, or with status {@code 400 (Bad Request)} if the comments has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CommentsDTO> createComments(@Valid @RequestBody CommentsDTO commentsDTO) throws URISyntaxException {
        LOG.debug("REST request to save Comments : {}", commentsDTO);
        if (commentsDTO.getId() != null) {
            throw new BadRequestAlertException("A new comments cannot already have an ID", ENTITY_NAME, "idexists");
        }
        commentsDTO = commentsService.save(commentsDTO);
        return ResponseEntity.created(new URI("/api/comments/" + commentsDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, commentsDTO.getId().toString()))
            .body(commentsDTO);
    }

    /**
     * {@code PUT  /comments/:id} : Updates an existing comments.
     *
     * @param id the id of the commentsDTO to save.
     * @param commentsDTO the commentsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated commentsDTO,
     * or with status {@code 400 (Bad Request)} if the commentsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the commentsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommentsDTO> updateComments(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CommentsDTO commentsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Comments : {}, {}", id, commentsDTO);
        if (commentsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, commentsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!commentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        commentsDTO = commentsService.update(commentsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, commentsDTO.getId().toString()))
            .body(commentsDTO);
    }

    /**
     * {@code PATCH  /comments/:id} : Partial updates given fields of an existing comments, field will ignore if it is null
     *
     * @param id the id of the commentsDTO to save.
     * @param commentsDTO the commentsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated commentsDTO,
     * or with status {@code 400 (Bad Request)} if the commentsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the commentsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the commentsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CommentsDTO> partialUpdateComments(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CommentsDTO commentsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Comments partially : {}, {}", id, commentsDTO);
        if (commentsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, commentsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!commentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CommentsDTO> result = commentsService.partialUpdate(commentsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, commentsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /comments} : get all the comments.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of comments in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CommentsDTO>> getAllComments(
        CommentsCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Comments by criteria: {}", criteria);

        Page<CommentsDTO> page = commentsQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /comments/count} : count all the comments.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countComments(CommentsCriteria criteria) {
        LOG.debug("REST request to count Comments by criteria: {}", criteria);
        return ResponseEntity.ok().body(commentsQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /comments/:id} : get the "id" comments.
     *
     * @param id the id of the commentsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the commentsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommentsDTO> getComments(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Comments : {}", id);
        Optional<CommentsDTO> commentsDTO = commentsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(commentsDTO);
    }

    /**
     * {@code DELETE  /comments/:id} : delete the "id" comments.
     *
     * @param id the id of the commentsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComments(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Comments : {}", id);
        commentsService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
