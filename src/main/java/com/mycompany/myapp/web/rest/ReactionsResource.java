package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.ReactionsRepository;
import com.mycompany.myapp.service.ReactionsQueryService;
import com.mycompany.myapp.service.ReactionsService;
import com.mycompany.myapp.service.criteria.ReactionsCriteria;
import com.mycompany.myapp.service.dto.ReactionsDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.Reactions}.
 */
@RestController
@RequestMapping("/api/reactions")
public class ReactionsResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionsResource.class);

    private static final String ENTITY_NAME = "msMediaReactions";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReactionsService reactionsService;

    private final ReactionsRepository reactionsRepository;

    private final ReactionsQueryService reactionsQueryService;

    public ReactionsResource(
        ReactionsService reactionsService,
        ReactionsRepository reactionsRepository,
        ReactionsQueryService reactionsQueryService
    ) {
        this.reactionsService = reactionsService;
        this.reactionsRepository = reactionsRepository;
        this.reactionsQueryService = reactionsQueryService;
    }

    /**
     * {@code POST  /reactions} : Create a new reactions.
     *
     * @param reactionsDTO the reactionsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reactionsDTO, or with status {@code 400 (Bad Request)} if the reactions has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ReactionsDTO> createReactions(@RequestBody ReactionsDTO reactionsDTO) throws URISyntaxException {
        LOG.debug("REST request to save Reactions : {}", reactionsDTO);
        if (reactionsDTO.getId() != null) {
            throw new BadRequestAlertException("A new reactions cannot already have an ID", ENTITY_NAME, "idexists");
        }
        reactionsDTO = reactionsService.save(reactionsDTO);
        return ResponseEntity.created(new URI("/api/reactions/" + reactionsDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, reactionsDTO.getId().toString()))
            .body(reactionsDTO);
    }

    /**
     * {@code PUT  /reactions/:id} : Updates an existing reactions.
     *
     * @param id the id of the reactionsDTO to save.
     * @param reactionsDTO the reactionsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reactionsDTO,
     * or with status {@code 400 (Bad Request)} if the reactionsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reactionsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReactionsDTO> updateReactions(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ReactionsDTO reactionsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Reactions : {}, {}", id, reactionsDTO);
        if (reactionsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reactionsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reactionsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        reactionsDTO = reactionsService.update(reactionsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reactionsDTO.getId().toString()))
            .body(reactionsDTO);
    }

    /**
     * {@code PATCH  /reactions/:id} : Partial updates given fields of an existing reactions, field will ignore if it is null
     *
     * @param id the id of the reactionsDTO to save.
     * @param reactionsDTO the reactionsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reactionsDTO,
     * or with status {@code 400 (Bad Request)} if the reactionsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reactionsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reactionsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReactionsDTO> partialUpdateReactions(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ReactionsDTO reactionsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Reactions partially : {}, {}", id, reactionsDTO);
        if (reactionsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reactionsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reactionsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReactionsDTO> result = reactionsService.partialUpdate(reactionsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reactionsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /reactions} : get all the reactions.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reactions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ReactionsDTO>> getAllReactions(
        ReactionsCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Reactions by criteria: {}", criteria);

        Page<ReactionsDTO> page = reactionsQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reactions/count} : count all the reactions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countReactions(ReactionsCriteria criteria) {
        LOG.debug("REST request to count Reactions by criteria: {}", criteria);
        return ResponseEntity.ok().body(reactionsQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /reactions/:id} : get the "id" reactions.
     *
     * @param id the id of the reactionsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reactionsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReactionsDTO> getReactions(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Reactions : {}", id);
        Optional<ReactionsDTO> reactionsDTO = reactionsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reactionsDTO);
    }

    /**
     * {@code DELETE  /reactions/:id} : delete the "id" reactions.
     *
     * @param id the id of the reactionsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReactions(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Reactions : {}", id);
        reactionsService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
