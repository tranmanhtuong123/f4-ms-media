package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.ReportedCommentsAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Comments;
import com.mycompany.myapp.domain.ReportedComments;
import com.mycompany.myapp.repository.ReportedCommentsRepository;
import com.mycompany.myapp.service.dto.ReportedCommentsDTO;
import com.mycompany.myapp.service.mapper.ReportedCommentsMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ReportedCommentsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ReportedCommentsResourceIT {

    private static final Long DEFAULT_REPORTED_BY = 1L;
    private static final Long UPDATED_REPORTED_BY = 2L;
    private static final Long SMALLER_REPORTED_BY = 1L - 1L;

    private static final String DEFAULT_REASON = "AAAAAAAAAA";
    private static final String UPDATED_REASON = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/reported-comments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ReportedCommentsRepository reportedCommentsRepository;

    @Autowired
    private ReportedCommentsMapper reportedCommentsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReportedCommentsMockMvc;

    private ReportedComments reportedComments;

    private ReportedComments insertedReportedComments;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReportedComments createEntity() {
        return new ReportedComments().reportedBy(DEFAULT_REPORTED_BY).reason(DEFAULT_REASON).createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ReportedComments createUpdatedEntity() {
        return new ReportedComments().reportedBy(UPDATED_REPORTED_BY).reason(UPDATED_REASON).createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    public void initTest() {
        reportedComments = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedReportedComments != null) {
            reportedCommentsRepository.delete(insertedReportedComments);
            insertedReportedComments = null;
        }
    }

    @Test
    @Transactional
    void createReportedComments() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ReportedComments
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(reportedComments);
        var returnedReportedCommentsDTO = om.readValue(
            restReportedCommentsMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(reportedCommentsDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ReportedCommentsDTO.class
        );

        // Validate the ReportedComments in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedReportedComments = reportedCommentsMapper.toEntity(returnedReportedCommentsDTO);
        assertReportedCommentsUpdatableFieldsEquals(returnedReportedComments, getPersistedReportedComments(returnedReportedComments));

        insertedReportedComments = returnedReportedComments;
    }

    @Test
    @Transactional
    void createReportedCommentsWithExistingId() throws Exception {
        // Create the ReportedComments with an existing ID
        reportedComments.setId(1L);
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(reportedComments);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReportedCommentsMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reportedCommentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportedComments in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllReportedComments() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList
        restReportedCommentsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reportedComments.getId().intValue())))
            .andExpect(jsonPath("$.[*].reportedBy").value(hasItem(DEFAULT_REPORTED_BY.intValue())))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getReportedComments() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get the reportedComments
        restReportedCommentsMockMvc
            .perform(get(ENTITY_API_URL_ID, reportedComments.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reportedComments.getId().intValue()))
            .andExpect(jsonPath("$.reportedBy").value(DEFAULT_REPORTED_BY.intValue()))
            .andExpect(jsonPath("$.reason").value(DEFAULT_REASON))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getReportedCommentsByIdFiltering() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        Long id = reportedComments.getId();

        defaultReportedCommentsFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultReportedCommentsFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultReportedCommentsFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReportedByIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reportedBy equals to
        defaultReportedCommentsFiltering("reportedBy.equals=" + DEFAULT_REPORTED_BY, "reportedBy.equals=" + UPDATED_REPORTED_BY);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReportedByIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reportedBy in
        defaultReportedCommentsFiltering(
            "reportedBy.in=" + DEFAULT_REPORTED_BY + "," + UPDATED_REPORTED_BY,
            "reportedBy.in=" + UPDATED_REPORTED_BY
        );
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReportedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reportedBy is not null
        defaultReportedCommentsFiltering("reportedBy.specified=true", "reportedBy.specified=false");
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReportedByIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reportedBy is greater than or equal to
        defaultReportedCommentsFiltering(
            "reportedBy.greaterThanOrEqual=" + DEFAULT_REPORTED_BY,
            "reportedBy.greaterThanOrEqual=" + UPDATED_REPORTED_BY
        );
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReportedByIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reportedBy is less than or equal to
        defaultReportedCommentsFiltering(
            "reportedBy.lessThanOrEqual=" + DEFAULT_REPORTED_BY,
            "reportedBy.lessThanOrEqual=" + SMALLER_REPORTED_BY
        );
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReportedByIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reportedBy is less than
        defaultReportedCommentsFiltering("reportedBy.lessThan=" + UPDATED_REPORTED_BY, "reportedBy.lessThan=" + DEFAULT_REPORTED_BY);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReportedByIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reportedBy is greater than
        defaultReportedCommentsFiltering("reportedBy.greaterThan=" + SMALLER_REPORTED_BY, "reportedBy.greaterThan=" + DEFAULT_REPORTED_BY);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReasonIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reason equals to
        defaultReportedCommentsFiltering("reason.equals=" + DEFAULT_REASON, "reason.equals=" + UPDATED_REASON);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReasonIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reason in
        defaultReportedCommentsFiltering("reason.in=" + DEFAULT_REASON + "," + UPDATED_REASON, "reason.in=" + UPDATED_REASON);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReasonIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reason is not null
        defaultReportedCommentsFiltering("reason.specified=true", "reason.specified=false");
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReasonContainsSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reason contains
        defaultReportedCommentsFiltering("reason.contains=" + DEFAULT_REASON, "reason.contains=" + UPDATED_REASON);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByReasonNotContainsSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where reason does not contain
        defaultReportedCommentsFiltering("reason.doesNotContain=" + UPDATED_REASON, "reason.doesNotContain=" + DEFAULT_REASON);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where createdAt equals to
        defaultReportedCommentsFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllReportedCommentsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where createdAt in
        defaultReportedCommentsFiltering(
            "createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT,
            "createdAt.in=" + UPDATED_CREATED_AT
        );
    }

    @Test
    @Transactional
    void getAllReportedCommentsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        // Get all the reportedCommentsList where createdAt is not null
        defaultReportedCommentsFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllReportedCommentsByCommentIsEqualToSomething() throws Exception {
        Comments comment;
        if (TestUtil.findAll(em, Comments.class).isEmpty()) {
            reportedCommentsRepository.saveAndFlush(reportedComments);
            comment = CommentsResourceIT.createEntity();
        } else {
            comment = TestUtil.findAll(em, Comments.class).get(0);
        }
        em.persist(comment);
        em.flush();
        reportedComments.setComment(comment);
        reportedCommentsRepository.saveAndFlush(reportedComments);
        Long commentId = comment.getId();
        // Get all the reportedCommentsList where comment equals to commentId
        defaultReportedCommentsShouldBeFound("commentId.equals=" + commentId);

        // Get all the reportedCommentsList where comment equals to (commentId + 1)
        defaultReportedCommentsShouldNotBeFound("commentId.equals=" + (commentId + 1));
    }

    private void defaultReportedCommentsFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultReportedCommentsShouldBeFound(shouldBeFound);
        defaultReportedCommentsShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultReportedCommentsShouldBeFound(String filter) throws Exception {
        restReportedCommentsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reportedComments.getId().intValue())))
            .andExpect(jsonPath("$.[*].reportedBy").value(hasItem(DEFAULT_REPORTED_BY.intValue())))
            .andExpect(jsonPath("$.[*].reason").value(hasItem(DEFAULT_REASON)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restReportedCommentsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultReportedCommentsShouldNotBeFound(String filter) throws Exception {
        restReportedCommentsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReportedCommentsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingReportedComments() throws Exception {
        // Get the reportedComments
        restReportedCommentsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReportedComments() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reportedComments
        ReportedComments updatedReportedComments = reportedCommentsRepository.findById(reportedComments.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedReportedComments are not directly saved in db
        em.detach(updatedReportedComments);
        updatedReportedComments.reportedBy(UPDATED_REPORTED_BY).reason(UPDATED_REASON).createdAt(UPDATED_CREATED_AT);
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(updatedReportedComments);

        restReportedCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reportedCommentsDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reportedCommentsDTO))
            )
            .andExpect(status().isOk());

        // Validate the ReportedComments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedReportedCommentsToMatchAllProperties(updatedReportedComments);
    }

    @Test
    @Transactional
    void putNonExistingReportedComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reportedComments.setId(longCount.incrementAndGet());

        // Create the ReportedComments
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(reportedComments);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReportedCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reportedCommentsDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reportedCommentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportedComments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReportedComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reportedComments.setId(longCount.incrementAndGet());

        // Create the ReportedComments
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(reportedComments);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportedCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reportedCommentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportedComments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReportedComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reportedComments.setId(longCount.incrementAndGet());

        // Create the ReportedComments
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(reportedComments);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportedCommentsMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reportedCommentsDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReportedComments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReportedCommentsWithPatch() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reportedComments using partial update
        ReportedComments partialUpdatedReportedComments = new ReportedComments();
        partialUpdatedReportedComments.setId(reportedComments.getId());

        partialUpdatedReportedComments.reportedBy(UPDATED_REPORTED_BY);

        restReportedCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReportedComments.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReportedComments))
            )
            .andExpect(status().isOk());

        // Validate the ReportedComments in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReportedCommentsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedReportedComments, reportedComments),
            getPersistedReportedComments(reportedComments)
        );
    }

    @Test
    @Transactional
    void fullUpdateReportedCommentsWithPatch() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reportedComments using partial update
        ReportedComments partialUpdatedReportedComments = new ReportedComments();
        partialUpdatedReportedComments.setId(reportedComments.getId());

        partialUpdatedReportedComments.reportedBy(UPDATED_REPORTED_BY).reason(UPDATED_REASON).createdAt(UPDATED_CREATED_AT);

        restReportedCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReportedComments.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReportedComments))
            )
            .andExpect(status().isOk());

        // Validate the ReportedComments in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReportedCommentsUpdatableFieldsEquals(
            partialUpdatedReportedComments,
            getPersistedReportedComments(partialUpdatedReportedComments)
        );
    }

    @Test
    @Transactional
    void patchNonExistingReportedComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reportedComments.setId(longCount.incrementAndGet());

        // Create the ReportedComments
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(reportedComments);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReportedCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reportedCommentsDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reportedCommentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportedComments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReportedComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reportedComments.setId(longCount.incrementAndGet());

        // Create the ReportedComments
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(reportedComments);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportedCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reportedCommentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ReportedComments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReportedComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reportedComments.setId(longCount.incrementAndGet());

        // Create the ReportedComments
        ReportedCommentsDTO reportedCommentsDTO = reportedCommentsMapper.toDto(reportedComments);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReportedCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reportedCommentsDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ReportedComments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReportedComments() throws Exception {
        // Initialize the database
        insertedReportedComments = reportedCommentsRepository.saveAndFlush(reportedComments);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the reportedComments
        restReportedCommentsMockMvc
            .perform(delete(ENTITY_API_URL_ID, reportedComments.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return reportedCommentsRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ReportedComments getPersistedReportedComments(ReportedComments reportedComments) {
        return reportedCommentsRepository.findById(reportedComments.getId()).orElseThrow();
    }

    protected void assertPersistedReportedCommentsToMatchAllProperties(ReportedComments expectedReportedComments) {
        assertReportedCommentsAllPropertiesEquals(expectedReportedComments, getPersistedReportedComments(expectedReportedComments));
    }

    protected void assertPersistedReportedCommentsToMatchUpdatableProperties(ReportedComments expectedReportedComments) {
        assertReportedCommentsAllUpdatablePropertiesEquals(
            expectedReportedComments,
            getPersistedReportedComments(expectedReportedComments)
        );
    }
}
