package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.CommentsAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Comments;
import com.mycompany.myapp.domain.Comments;
import com.mycompany.myapp.domain.Posts;
import com.mycompany.myapp.repository.CommentsRepository;
import com.mycompany.myapp.service.dto.CommentsDTO;
import com.mycompany.myapp.service.mapper.CommentsMapper;
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
 * Integration tests for the {@link CommentsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CommentsResourceIT {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long UPDATED_USER_ID = 2L;
    private static final Long SMALLER_USER_ID = 1L - 1L;

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/comments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCommentsMockMvc;

    private Comments comments;

    private Comments insertedComments;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comments createEntity() {
        return new Comments().userId(DEFAULT_USER_ID).content(DEFAULT_CONTENT).createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comments createUpdatedEntity() {
        return new Comments().userId(UPDATED_USER_ID).content(UPDATED_CONTENT).createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    public void initTest() {
        comments = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedComments != null) {
            commentsRepository.delete(insertedComments);
            insertedComments = null;
        }
    }

    @Test
    @Transactional
    void createComments() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Comments
        CommentsDTO commentsDTO = commentsMapper.toDto(comments);
        var returnedCommentsDTO = om.readValue(
            restCommentsMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commentsDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CommentsDTO.class
        );

        // Validate the Comments in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedComments = commentsMapper.toEntity(returnedCommentsDTO);
        assertCommentsUpdatableFieldsEquals(returnedComments, getPersistedComments(returnedComments));

        insertedComments = returnedComments;
    }

    @Test
    @Transactional
    void createCommentsWithExistingId() throws Exception {
        // Create the Comments with an existing ID
        comments.setId(1L);
        CommentsDTO commentsDTO = commentsMapper.toDto(comments);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCommentsMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commentsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllComments() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList
        restCommentsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comments.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getComments() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get the comments
        restCommentsMockMvc
            .perform(get(ENTITY_API_URL_ID, comments.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(comments.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.intValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getCommentsByIdFiltering() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        Long id = comments.getId();

        defaultCommentsFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCommentsFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCommentsFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCommentsByUserIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where userId equals to
        defaultCommentsFiltering("userId.equals=" + DEFAULT_USER_ID, "userId.equals=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllCommentsByUserIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where userId in
        defaultCommentsFiltering("userId.in=" + DEFAULT_USER_ID + "," + UPDATED_USER_ID, "userId.in=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllCommentsByUserIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where userId is not null
        defaultCommentsFiltering("userId.specified=true", "userId.specified=false");
    }

    @Test
    @Transactional
    void getAllCommentsByUserIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where userId is greater than or equal to
        defaultCommentsFiltering("userId.greaterThanOrEqual=" + DEFAULT_USER_ID, "userId.greaterThanOrEqual=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllCommentsByUserIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where userId is less than or equal to
        defaultCommentsFiltering("userId.lessThanOrEqual=" + DEFAULT_USER_ID, "userId.lessThanOrEqual=" + SMALLER_USER_ID);
    }

    @Test
    @Transactional
    void getAllCommentsByUserIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where userId is less than
        defaultCommentsFiltering("userId.lessThan=" + UPDATED_USER_ID, "userId.lessThan=" + DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    void getAllCommentsByUserIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where userId is greater than
        defaultCommentsFiltering("userId.greaterThan=" + SMALLER_USER_ID, "userId.greaterThan=" + DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    void getAllCommentsByContentIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where content equals to
        defaultCommentsFiltering("content.equals=" + DEFAULT_CONTENT, "content.equals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllCommentsByContentIsInShouldWork() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where content in
        defaultCommentsFiltering("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT, "content.in=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllCommentsByContentIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where content is not null
        defaultCommentsFiltering("content.specified=true", "content.specified=false");
    }

    @Test
    @Transactional
    void getAllCommentsByContentContainsSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where content contains
        defaultCommentsFiltering("content.contains=" + DEFAULT_CONTENT, "content.contains=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllCommentsByContentNotContainsSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where content does not contain
        defaultCommentsFiltering("content.doesNotContain=" + UPDATED_CONTENT, "content.doesNotContain=" + DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    void getAllCommentsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where createdAt equals to
        defaultCommentsFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllCommentsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where createdAt in
        defaultCommentsFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllCommentsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        // Get all the commentsList where createdAt is not null
        defaultCommentsFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllCommentsByPostIsEqualToSomething() throws Exception {
        Posts post;
        if (TestUtil.findAll(em, Posts.class).isEmpty()) {
            commentsRepository.saveAndFlush(comments);
            post = PostsResourceIT.createEntity();
        } else {
            post = TestUtil.findAll(em, Posts.class).get(0);
        }
        em.persist(post);
        em.flush();
        comments.setPost(post);
        commentsRepository.saveAndFlush(comments);
        Long postId = post.getId();
        // Get all the commentsList where post equals to postId
        defaultCommentsShouldBeFound("postId.equals=" + postId);

        // Get all the commentsList where post equals to (postId + 1)
        defaultCommentsShouldNotBeFound("postId.equals=" + (postId + 1));
    }

    @Test
    @Transactional
    void getAllCommentsByParentCommentIsEqualToSomething() throws Exception {
        Comments parentComment;
        if (TestUtil.findAll(em, Comments.class).isEmpty()) {
            commentsRepository.saveAndFlush(comments);
            parentComment = CommentsResourceIT.createEntity();
        } else {
            parentComment = TestUtil.findAll(em, Comments.class).get(0);
        }
        em.persist(parentComment);
        em.flush();
        comments.setParentComment(parentComment);
        commentsRepository.saveAndFlush(comments);
        Long parentCommentId = parentComment.getId();
        // Get all the commentsList where parentComment equals to parentCommentId
        defaultCommentsShouldBeFound("parentCommentId.equals=" + parentCommentId);

        // Get all the commentsList where parentComment equals to (parentCommentId + 1)
        defaultCommentsShouldNotBeFound("parentCommentId.equals=" + (parentCommentId + 1));
    }

    private void defaultCommentsFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCommentsShouldBeFound(shouldBeFound);
        defaultCommentsShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCommentsShouldBeFound(String filter) throws Exception {
        restCommentsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(comments.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restCommentsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCommentsShouldNotBeFound(String filter) throws Exception {
        restCommentsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCommentsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingComments() throws Exception {
        // Get the comments
        restCommentsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingComments() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comments
        Comments updatedComments = commentsRepository.findById(comments.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedComments are not directly saved in db
        em.detach(updatedComments);
        updatedComments.userId(UPDATED_USER_ID).content(UPDATED_CONTENT).createdAt(UPDATED_CREATED_AT);
        CommentsDTO commentsDTO = commentsMapper.toDto(updatedComments);

        restCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, commentsDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(commentsDTO))
            )
            .andExpect(status().isOk());

        // Validate the Comments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCommentsToMatchAllProperties(updatedComments);
    }

    @Test
    @Transactional
    void putNonExistingComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comments.setId(longCount.incrementAndGet());

        // Create the Comments
        CommentsDTO commentsDTO = commentsMapper.toDto(comments);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, commentsDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(commentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comments.setId(longCount.incrementAndGet());

        // Create the Comments
        CommentsDTO commentsDTO = commentsMapper.toDto(comments);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(commentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comments.setId(longCount.incrementAndGet());

        // Create the Comments
        CommentsDTO commentsDTO = commentsMapper.toDto(comments);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(commentsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Comments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCommentsWithPatch() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comments using partial update
        Comments partialUpdatedComments = new Comments();
        partialUpdatedComments.setId(comments.getId());

        partialUpdatedComments.userId(UPDATED_USER_ID).content(UPDATED_CONTENT).createdAt(UPDATED_CREATED_AT);

        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedComments.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedComments))
            )
            .andExpect(status().isOk());

        // Validate the Comments in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCommentsUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedComments, comments), getPersistedComments(comments));
    }

    @Test
    @Transactional
    void fullUpdateCommentsWithPatch() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comments using partial update
        Comments partialUpdatedComments = new Comments();
        partialUpdatedComments.setId(comments.getId());

        partialUpdatedComments.userId(UPDATED_USER_ID).content(UPDATED_CONTENT).createdAt(UPDATED_CREATED_AT);

        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedComments.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedComments))
            )
            .andExpect(status().isOk());

        // Validate the Comments in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCommentsUpdatableFieldsEquals(partialUpdatedComments, getPersistedComments(partialUpdatedComments));
    }

    @Test
    @Transactional
    void patchNonExistingComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comments.setId(longCount.incrementAndGet());

        // Create the Comments
        CommentsDTO commentsDTO = commentsMapper.toDto(comments);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, commentsDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(commentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comments.setId(longCount.incrementAndGet());

        // Create the Comments
        CommentsDTO commentsDTO = commentsMapper.toDto(comments);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(commentsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Comments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamComments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comments.setId(longCount.incrementAndGet());

        // Create the Comments
        CommentsDTO commentsDTO = commentsMapper.toDto(comments);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCommentsMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(commentsDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Comments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteComments() throws Exception {
        // Initialize the database
        insertedComments = commentsRepository.saveAndFlush(comments);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the comments
        restCommentsMockMvc
            .perform(delete(ENTITY_API_URL_ID, comments.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return commentsRepository.count();
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

    protected Comments getPersistedComments(Comments comments) {
        return commentsRepository.findById(comments.getId()).orElseThrow();
    }

    protected void assertPersistedCommentsToMatchAllProperties(Comments expectedComments) {
        assertCommentsAllPropertiesEquals(expectedComments, getPersistedComments(expectedComments));
    }

    protected void assertPersistedCommentsToMatchUpdatableProperties(Comments expectedComments) {
        assertCommentsAllUpdatablePropertiesEquals(expectedComments, getPersistedComments(expectedComments));
    }
}
