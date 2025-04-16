package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.ReactionsAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Comments;
import com.mycompany.myapp.domain.Posts;
import com.mycompany.myapp.domain.Reactions;
import com.mycompany.myapp.domain.enumeration.ReactionType;
import com.mycompany.myapp.repository.ReactionsRepository;
import com.mycompany.myapp.service.dto.ReactionsDTO;
import com.mycompany.myapp.service.mapper.ReactionsMapper;
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
 * Integration tests for the {@link ReactionsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ReactionsResourceIT {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long UPDATED_USER_ID = 2L;
    private static final Long SMALLER_USER_ID = 1L - 1L;

    private static final ReactionType DEFAULT_REACTION_TYPE = ReactionType.LIKE;
    private static final ReactionType UPDATED_REACTION_TYPE = ReactionType.LOVE;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/reactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ReactionsRepository reactionsRepository;

    @Autowired
    private ReactionsMapper reactionsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReactionsMockMvc;

    private Reactions reactions;

    private Reactions insertedReactions;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reactions createEntity() {
        return new Reactions().userId(DEFAULT_USER_ID).reactionType(DEFAULT_REACTION_TYPE).createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reactions createUpdatedEntity() {
        return new Reactions().userId(UPDATED_USER_ID).reactionType(UPDATED_REACTION_TYPE).createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    public void initTest() {
        reactions = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedReactions != null) {
            reactionsRepository.delete(insertedReactions);
            insertedReactions = null;
        }
    }

    @Test
    @Transactional
    void createReactions() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Reactions
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(reactions);
        var returnedReactionsDTO = om.readValue(
            restReactionsMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionsDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ReactionsDTO.class
        );

        // Validate the Reactions in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedReactions = reactionsMapper.toEntity(returnedReactionsDTO);
        assertReactionsUpdatableFieldsEquals(returnedReactions, getPersistedReactions(returnedReactions));

        insertedReactions = returnedReactions;
    }

    @Test
    @Transactional
    void createReactionsWithExistingId() throws Exception {
        // Create the Reactions with an existing ID
        reactions.setId(1L);
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(reactions);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReactionsMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Reactions in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllReactions() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList
        restReactionsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reactions.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].reactionType").value(hasItem(DEFAULT_REACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getReactions() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get the reactions
        restReactionsMockMvc
            .perform(get(ENTITY_API_URL_ID, reactions.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reactions.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.intValue()))
            .andExpect(jsonPath("$.reactionType").value(DEFAULT_REACTION_TYPE.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getReactionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        Long id = reactions.getId();

        defaultReactionsFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultReactionsFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultReactionsFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllReactionsByUserIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where userId equals to
        defaultReactionsFiltering("userId.equals=" + DEFAULT_USER_ID, "userId.equals=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllReactionsByUserIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where userId in
        defaultReactionsFiltering("userId.in=" + DEFAULT_USER_ID + "," + UPDATED_USER_ID, "userId.in=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllReactionsByUserIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where userId is not null
        defaultReactionsFiltering("userId.specified=true", "userId.specified=false");
    }

    @Test
    @Transactional
    void getAllReactionsByUserIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where userId is greater than or equal to
        defaultReactionsFiltering("userId.greaterThanOrEqual=" + DEFAULT_USER_ID, "userId.greaterThanOrEqual=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllReactionsByUserIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where userId is less than or equal to
        defaultReactionsFiltering("userId.lessThanOrEqual=" + DEFAULT_USER_ID, "userId.lessThanOrEqual=" + SMALLER_USER_ID);
    }

    @Test
    @Transactional
    void getAllReactionsByUserIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where userId is less than
        defaultReactionsFiltering("userId.lessThan=" + UPDATED_USER_ID, "userId.lessThan=" + DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    void getAllReactionsByUserIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where userId is greater than
        defaultReactionsFiltering("userId.greaterThan=" + SMALLER_USER_ID, "userId.greaterThan=" + DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    void getAllReactionsByReactionTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where reactionType equals to
        defaultReactionsFiltering("reactionType.equals=" + DEFAULT_REACTION_TYPE, "reactionType.equals=" + UPDATED_REACTION_TYPE);
    }

    @Test
    @Transactional
    void getAllReactionsByReactionTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where reactionType in
        defaultReactionsFiltering(
            "reactionType.in=" + DEFAULT_REACTION_TYPE + "," + UPDATED_REACTION_TYPE,
            "reactionType.in=" + UPDATED_REACTION_TYPE
        );
    }

    @Test
    @Transactional
    void getAllReactionsByReactionTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where reactionType is not null
        defaultReactionsFiltering("reactionType.specified=true", "reactionType.specified=false");
    }

    @Test
    @Transactional
    void getAllReactionsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where createdAt equals to
        defaultReactionsFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllReactionsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where createdAt in
        defaultReactionsFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllReactionsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        // Get all the reactionsList where createdAt is not null
        defaultReactionsFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllReactionsByPostIsEqualToSomething() throws Exception {
        Posts post;
        if (TestUtil.findAll(em, Posts.class).isEmpty()) {
            reactionsRepository.saveAndFlush(reactions);
            post = PostsResourceIT.createEntity();
        } else {
            post = TestUtil.findAll(em, Posts.class).get(0);
        }
        em.persist(post);
        em.flush();
        reactions.setPost(post);
        reactionsRepository.saveAndFlush(reactions);
        Long postId = post.getId();
        // Get all the reactionsList where post equals to postId
        defaultReactionsShouldBeFound("postId.equals=" + postId);

        // Get all the reactionsList where post equals to (postId + 1)
        defaultReactionsShouldNotBeFound("postId.equals=" + (postId + 1));
    }

    @Test
    @Transactional
    void getAllReactionsByCommentIsEqualToSomething() throws Exception {
        Comments comment;
        if (TestUtil.findAll(em, Comments.class).isEmpty()) {
            reactionsRepository.saveAndFlush(reactions);
            comment = CommentsResourceIT.createEntity();
        } else {
            comment = TestUtil.findAll(em, Comments.class).get(0);
        }
        em.persist(comment);
        em.flush();
        reactions.setComment(comment);
        reactionsRepository.saveAndFlush(reactions);
        Long commentId = comment.getId();
        // Get all the reactionsList where comment equals to commentId
        defaultReactionsShouldBeFound("commentId.equals=" + commentId);

        // Get all the reactionsList where comment equals to (commentId + 1)
        defaultReactionsShouldNotBeFound("commentId.equals=" + (commentId + 1));
    }

    private void defaultReactionsFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultReactionsShouldBeFound(shouldBeFound);
        defaultReactionsShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultReactionsShouldBeFound(String filter) throws Exception {
        restReactionsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reactions.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].reactionType").value(hasItem(DEFAULT_REACTION_TYPE.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));

        // Check, that the count call also returns 1
        restReactionsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultReactionsShouldNotBeFound(String filter) throws Exception {
        restReactionsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReactionsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingReactions() throws Exception {
        // Get the reactions
        restReactionsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReactions() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reactions
        Reactions updatedReactions = reactionsRepository.findById(reactions.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedReactions are not directly saved in db
        em.detach(updatedReactions);
        updatedReactions.userId(UPDATED_USER_ID).reactionType(UPDATED_REACTION_TYPE).createdAt(UPDATED_CREATED_AT);
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(updatedReactions);

        restReactionsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reactionsDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionsDTO))
            )
            .andExpect(status().isOk());

        // Validate the Reactions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedReactionsToMatchAllProperties(updatedReactions);
    }

    @Test
    @Transactional
    void putNonExistingReactions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reactions.setId(longCount.incrementAndGet());

        // Create the Reactions
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(reactions);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReactionsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reactionsDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reactions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReactions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reactions.setId(longCount.incrementAndGet());

        // Create the Reactions
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(reactions);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reactionsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reactions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReactions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reactions.setId(longCount.incrementAndGet());

        // Create the Reactions
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(reactions);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionsMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reactionsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reactions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReactionsWithPatch() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reactions using partial update
        Reactions partialUpdatedReactions = new Reactions();
        partialUpdatedReactions.setId(reactions.getId());

        restReactionsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReactions.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReactions))
            )
            .andExpect(status().isOk());

        // Validate the Reactions in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReactionsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedReactions, reactions),
            getPersistedReactions(reactions)
        );
    }

    @Test
    @Transactional
    void fullUpdateReactionsWithPatch() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reactions using partial update
        Reactions partialUpdatedReactions = new Reactions();
        partialUpdatedReactions.setId(reactions.getId());

        partialUpdatedReactions.userId(UPDATED_USER_ID).reactionType(UPDATED_REACTION_TYPE).createdAt(UPDATED_CREATED_AT);

        restReactionsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReactions.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReactions))
            )
            .andExpect(status().isOk());

        // Validate the Reactions in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReactionsUpdatableFieldsEquals(partialUpdatedReactions, getPersistedReactions(partialUpdatedReactions));
    }

    @Test
    @Transactional
    void patchNonExistingReactions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reactions.setId(longCount.incrementAndGet());

        // Create the Reactions
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(reactions);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReactionsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reactionsDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reactionsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reactions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReactions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reactions.setId(longCount.incrementAndGet());

        // Create the Reactions
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(reactions);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reactionsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reactions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReactions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reactions.setId(longCount.incrementAndGet());

        // Create the Reactions
        ReactionsDTO reactionsDTO = reactionsMapper.toDto(reactions);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReactionsMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(reactionsDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reactions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReactions() throws Exception {
        // Initialize the database
        insertedReactions = reactionsRepository.saveAndFlush(reactions);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the reactions
        restReactionsMockMvc
            .perform(delete(ENTITY_API_URL_ID, reactions.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return reactionsRepository.count();
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

    protected Reactions getPersistedReactions(Reactions reactions) {
        return reactionsRepository.findById(reactions.getId()).orElseThrow();
    }

    protected void assertPersistedReactionsToMatchAllProperties(Reactions expectedReactions) {
        assertReactionsAllPropertiesEquals(expectedReactions, getPersistedReactions(expectedReactions));
    }

    protected void assertPersistedReactionsToMatchUpdatableProperties(Reactions expectedReactions) {
        assertReactionsAllUpdatablePropertiesEquals(expectedReactions, getPersistedReactions(expectedReactions));
    }
}
