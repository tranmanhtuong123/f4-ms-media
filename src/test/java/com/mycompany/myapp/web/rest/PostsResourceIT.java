package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.PostsAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Posts;
import com.mycompany.myapp.repository.PostsRepository;
import com.mycompany.myapp.service.dto.PostsDTO;
import com.mycompany.myapp.service.mapper.PostsMapper;
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
 * Integration tests for the {@link PostsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PostsResourceIT {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long UPDATED_USER_ID = 2L;
    private static final Long SMALLER_USER_ID = 1L - 1L;

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final String DEFAULT_MEDIA_URL = "AAAAAAAAAA";
    private static final String UPDATED_MEDIA_URL = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_PRIVATE = false;
    private static final Boolean UPDATED_IS_PRIVATE = true;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/posts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private PostsMapper postsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPostsMockMvc;

    private Posts posts;

    private Posts insertedPosts;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Posts createEntity() {
        return new Posts()
            .userId(DEFAULT_USER_ID)
            .content(DEFAULT_CONTENT)
            .mediaUrl(DEFAULT_MEDIA_URL)
            .isPrivate(DEFAULT_IS_PRIVATE)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Posts createUpdatedEntity() {
        return new Posts()
            .userId(UPDATED_USER_ID)
            .content(UPDATED_CONTENT)
            .mediaUrl(UPDATED_MEDIA_URL)
            .isPrivate(UPDATED_IS_PRIVATE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
    }

    @BeforeEach
    public void initTest() {
        posts = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedPosts != null) {
            postsRepository.delete(insertedPosts);
            insertedPosts = null;
        }
    }

    @Test
    @Transactional
    void createPosts() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Posts
        PostsDTO postsDTO = postsMapper.toDto(posts);
        var returnedPostsDTO = om.readValue(
            restPostsMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(postsDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PostsDTO.class
        );

        // Validate the Posts in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPosts = postsMapper.toEntity(returnedPostsDTO);
        assertPostsUpdatableFieldsEquals(returnedPosts, getPersistedPosts(returnedPosts));

        insertedPosts = returnedPosts;
    }

    @Test
    @Transactional
    void createPostsWithExistingId() throws Exception {
        // Create the Posts with an existing ID
        posts.setId(1L);
        PostsDTO postsDTO = postsMapper.toDto(posts);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPostsMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(postsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllPosts() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList
        restPostsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(posts.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].mediaUrl").value(hasItem(DEFAULT_MEDIA_URL)))
            .andExpect(jsonPath("$.[*].isPrivate").value(hasItem(DEFAULT_IS_PRIVATE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @Test
    @Transactional
    void getPosts() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get the posts
        restPostsMockMvc
            .perform(get(ENTITY_API_URL_ID, posts.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(posts.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.intValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.mediaUrl").value(DEFAULT_MEDIA_URL))
            .andExpect(jsonPath("$.isPrivate").value(DEFAULT_IS_PRIVATE))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getPostsByIdFiltering() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        Long id = posts.getId();

        defaultPostsFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultPostsFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultPostsFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllPostsByUserIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where userId equals to
        defaultPostsFiltering("userId.equals=" + DEFAULT_USER_ID, "userId.equals=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllPostsByUserIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where userId in
        defaultPostsFiltering("userId.in=" + DEFAULT_USER_ID + "," + UPDATED_USER_ID, "userId.in=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllPostsByUserIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where userId is not null
        defaultPostsFiltering("userId.specified=true", "userId.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByUserIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where userId is greater than or equal to
        defaultPostsFiltering("userId.greaterThanOrEqual=" + DEFAULT_USER_ID, "userId.greaterThanOrEqual=" + UPDATED_USER_ID);
    }

    @Test
    @Transactional
    void getAllPostsByUserIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where userId is less than or equal to
        defaultPostsFiltering("userId.lessThanOrEqual=" + DEFAULT_USER_ID, "userId.lessThanOrEqual=" + SMALLER_USER_ID);
    }

    @Test
    @Transactional
    void getAllPostsByUserIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where userId is less than
        defaultPostsFiltering("userId.lessThan=" + UPDATED_USER_ID, "userId.lessThan=" + DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    void getAllPostsByUserIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where userId is greater than
        defaultPostsFiltering("userId.greaterThan=" + SMALLER_USER_ID, "userId.greaterThan=" + DEFAULT_USER_ID);
    }

    @Test
    @Transactional
    void getAllPostsByContentIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where content equals to
        defaultPostsFiltering("content.equals=" + DEFAULT_CONTENT, "content.equals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllPostsByContentIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where content in
        defaultPostsFiltering("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT, "content.in=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllPostsByContentIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where content is not null
        defaultPostsFiltering("content.specified=true", "content.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByContentContainsSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where content contains
        defaultPostsFiltering("content.contains=" + DEFAULT_CONTENT, "content.contains=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    void getAllPostsByContentNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where content does not contain
        defaultPostsFiltering("content.doesNotContain=" + UPDATED_CONTENT, "content.doesNotContain=" + DEFAULT_CONTENT);
    }

    @Test
    @Transactional
    void getAllPostsByMediaUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where mediaUrl equals to
        defaultPostsFiltering("mediaUrl.equals=" + DEFAULT_MEDIA_URL, "mediaUrl.equals=" + UPDATED_MEDIA_URL);
    }

    @Test
    @Transactional
    void getAllPostsByMediaUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where mediaUrl in
        defaultPostsFiltering("mediaUrl.in=" + DEFAULT_MEDIA_URL + "," + UPDATED_MEDIA_URL, "mediaUrl.in=" + UPDATED_MEDIA_URL);
    }

    @Test
    @Transactional
    void getAllPostsByMediaUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where mediaUrl is not null
        defaultPostsFiltering("mediaUrl.specified=true", "mediaUrl.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByMediaUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where mediaUrl contains
        defaultPostsFiltering("mediaUrl.contains=" + DEFAULT_MEDIA_URL, "mediaUrl.contains=" + UPDATED_MEDIA_URL);
    }

    @Test
    @Transactional
    void getAllPostsByMediaUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where mediaUrl does not contain
        defaultPostsFiltering("mediaUrl.doesNotContain=" + UPDATED_MEDIA_URL, "mediaUrl.doesNotContain=" + DEFAULT_MEDIA_URL);
    }

    @Test
    @Transactional
    void getAllPostsByIsPrivateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where isPrivate equals to
        defaultPostsFiltering("isPrivate.equals=" + DEFAULT_IS_PRIVATE, "isPrivate.equals=" + UPDATED_IS_PRIVATE);
    }

    @Test
    @Transactional
    void getAllPostsByIsPrivateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where isPrivate in
        defaultPostsFiltering("isPrivate.in=" + DEFAULT_IS_PRIVATE + "," + UPDATED_IS_PRIVATE, "isPrivate.in=" + UPDATED_IS_PRIVATE);
    }

    @Test
    @Transactional
    void getAllPostsByIsPrivateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where isPrivate is not null
        defaultPostsFiltering("isPrivate.specified=true", "isPrivate.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where createdAt equals to
        defaultPostsFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where createdAt in
        defaultPostsFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where createdAt is not null
        defaultPostsFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where updatedAt equals to
        defaultPostsFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where updatedAt in
        defaultPostsFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        // Get all the postsList where updatedAt is not null
        defaultPostsFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    private void defaultPostsFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultPostsShouldBeFound(shouldBeFound);
        defaultPostsShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPostsShouldBeFound(String filter) throws Exception {
        restPostsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(posts.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].mediaUrl").value(hasItem(DEFAULT_MEDIA_URL)))
            .andExpect(jsonPath("$.[*].isPrivate").value(hasItem(DEFAULT_IS_PRIVATE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));

        // Check, that the count call also returns 1
        restPostsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPostsShouldNotBeFound(String filter) throws Exception {
        restPostsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPostsMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPosts() throws Exception {
        // Get the posts
        restPostsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPosts() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the posts
        Posts updatedPosts = postsRepository.findById(posts.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPosts are not directly saved in db
        em.detach(updatedPosts);
        updatedPosts
            .userId(UPDATED_USER_ID)
            .content(UPDATED_CONTENT)
            .mediaUrl(UPDATED_MEDIA_URL)
            .isPrivate(UPDATED_IS_PRIVATE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        PostsDTO postsDTO = postsMapper.toDto(updatedPosts);

        restPostsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, postsDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(postsDTO))
            )
            .andExpect(status().isOk());

        // Validate the Posts in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPostsToMatchAllProperties(updatedPosts);
    }

    @Test
    @Transactional
    void putNonExistingPosts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        posts.setId(longCount.incrementAndGet());

        // Create the Posts
        PostsDTO postsDTO = postsMapper.toDto(posts);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, postsDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(postsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPosts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        posts.setId(longCount.incrementAndGet());

        // Create the Posts
        PostsDTO postsDTO = postsMapper.toDto(posts);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(postsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPosts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        posts.setId(longCount.incrementAndGet());

        // Create the Posts
        PostsDTO postsDTO = postsMapper.toDto(posts);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(postsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Posts in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePostsWithPatch() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the posts using partial update
        Posts partialUpdatedPosts = new Posts();
        partialUpdatedPosts.setId(posts.getId());

        partialUpdatedPosts
            .userId(UPDATED_USER_ID)
            .content(UPDATED_CONTENT)
            .mediaUrl(UPDATED_MEDIA_URL)
            .isPrivate(UPDATED_IS_PRIVATE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restPostsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPosts.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPosts))
            )
            .andExpect(status().isOk());

        // Validate the Posts in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPostsUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPosts, posts), getPersistedPosts(posts));
    }

    @Test
    @Transactional
    void fullUpdatePostsWithPatch() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the posts using partial update
        Posts partialUpdatedPosts = new Posts();
        partialUpdatedPosts.setId(posts.getId());

        partialUpdatedPosts
            .userId(UPDATED_USER_ID)
            .content(UPDATED_CONTENT)
            .mediaUrl(UPDATED_MEDIA_URL)
            .isPrivate(UPDATED_IS_PRIVATE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restPostsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPosts.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPosts))
            )
            .andExpect(status().isOk());

        // Validate the Posts in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPostsUpdatableFieldsEquals(partialUpdatedPosts, getPersistedPosts(partialUpdatedPosts));
    }

    @Test
    @Transactional
    void patchNonExistingPosts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        posts.setId(longCount.incrementAndGet());

        // Create the Posts
        PostsDTO postsDTO = postsMapper.toDto(posts);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, postsDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(postsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPosts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        posts.setId(longCount.incrementAndGet());

        // Create the Posts
        PostsDTO postsDTO = postsMapper.toDto(posts);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(postsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Posts in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPosts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        posts.setId(longCount.incrementAndGet());

        // Create the Posts
        PostsDTO postsDTO = postsMapper.toDto(posts);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostsMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(postsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Posts in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePosts() throws Exception {
        // Initialize the database
        insertedPosts = postsRepository.saveAndFlush(posts);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the posts
        restPostsMockMvc
            .perform(delete(ENTITY_API_URL_ID, posts.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return postsRepository.count();
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

    protected Posts getPersistedPosts(Posts posts) {
        return postsRepository.findById(posts.getId()).orElseThrow();
    }

    protected void assertPersistedPostsToMatchAllProperties(Posts expectedPosts) {
        assertPostsAllPropertiesEquals(expectedPosts, getPersistedPosts(expectedPosts));
    }

    protected void assertPersistedPostsToMatchUpdatableProperties(Posts expectedPosts) {
        assertPostsAllUpdatablePropertiesEquals(expectedPosts, getPersistedPosts(expectedPosts));
    }
}
