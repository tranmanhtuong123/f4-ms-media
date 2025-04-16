package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.PostsRepository;
import com.mycompany.myapp.service.PostsQueryService;
import com.mycompany.myapp.service.PostsService;
import com.mycompany.myapp.service.criteria.PostsCriteria;
import com.mycompany.myapp.service.dto.PostsDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.Posts}.
 */
@RestController
@RequestMapping("/api/posts")
public class PostsResource {

    private static final Logger LOG = LoggerFactory.getLogger(PostsResource.class);

    private static final String ENTITY_NAME = "msMediaPosts";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PostsService postsService;

    private final PostsRepository postsRepository;

    private final PostsQueryService postsQueryService;

    public PostsResource(PostsService postsService, PostsRepository postsRepository, PostsQueryService postsQueryService) {
        this.postsService = postsService;
        this.postsRepository = postsRepository;
        this.postsQueryService = postsQueryService;
    }

    /**
     * {@code POST  /posts} : Create a new posts.
     *
     * @param postsDTO the postsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new postsDTO, or with status {@code 400 (Bad Request)} if the posts has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PostsDTO> createPosts(@Valid @RequestBody PostsDTO postsDTO) throws URISyntaxException {
        LOG.debug("REST request to save Posts : {}", postsDTO);
        if (postsDTO.getId() != null) {
            throw new BadRequestAlertException("A new posts cannot already have an ID", ENTITY_NAME, "idexists");
        }
        postsDTO = postsService.save(postsDTO);
        return ResponseEntity.created(new URI("/api/posts/" + postsDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, postsDTO.getId().toString()))
            .body(postsDTO);
    }

    /**
     * {@code PUT  /posts/:id} : Updates an existing posts.
     *
     * @param id the id of the postsDTO to save.
     * @param postsDTO the postsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated postsDTO,
     * or with status {@code 400 (Bad Request)} if the postsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the postsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostsDTO> updatePosts(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PostsDTO postsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Posts : {}, {}", id, postsDTO);
        if (postsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, postsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!postsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        postsDTO = postsService.update(postsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, postsDTO.getId().toString()))
            .body(postsDTO);
    }

    /**
     * {@code PATCH  /posts/:id} : Partial updates given fields of an existing posts, field will ignore if it is null
     *
     * @param id the id of the postsDTO to save.
     * @param postsDTO the postsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated postsDTO,
     * or with status {@code 400 (Bad Request)} if the postsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the postsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the postsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PostsDTO> partialUpdatePosts(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PostsDTO postsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Posts partially : {}, {}", id, postsDTO);
        if (postsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, postsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!postsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PostsDTO> result = postsService.partialUpdate(postsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, postsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /posts} : get all the posts.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of posts in body.
     */
    @GetMapping("")
    public ResponseEntity<List<PostsDTO>> getAllPosts(
        PostsCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Posts by criteria: {}", criteria);

        Page<PostsDTO> page = postsQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /posts/count} : count all the posts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countPosts(PostsCriteria criteria) {
        LOG.debug("REST request to count Posts by criteria: {}", criteria);
        return ResponseEntity.ok().body(postsQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /posts/:id} : get the "id" posts.
     *
     * @param id the id of the postsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the postsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostsDTO> getPosts(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Posts : {}", id);
        Optional<PostsDTO> postsDTO = postsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(postsDTO);
    }

    /**
     * {@code DELETE  /posts/:id} : delete the "id" posts.
     *
     * @param id the id of the postsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosts(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Posts : {}", id);
        postsService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
