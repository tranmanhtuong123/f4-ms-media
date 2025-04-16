package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Posts;
import com.mycompany.myapp.repository.PostsRepository;
import com.mycompany.myapp.service.dto.PostsDTO;
import com.mycompany.myapp.service.mapper.PostsMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Posts}.
 */
@Service
@Transactional
public class PostsService {

    private static final Logger LOG = LoggerFactory.getLogger(PostsService.class);

    private final PostsRepository postsRepository;

    private final PostsMapper postsMapper;

    public PostsService(PostsRepository postsRepository, PostsMapper postsMapper) {
        this.postsRepository = postsRepository;
        this.postsMapper = postsMapper;
    }

    /**
     * Save a posts.
     *
     * @param postsDTO the entity to save.
     * @return the persisted entity.
     */
    public PostsDTO save(PostsDTO postsDTO) {
        LOG.debug("Request to save Posts : {}", postsDTO);
        Posts posts = postsMapper.toEntity(postsDTO);
        posts = postsRepository.save(posts);
        return postsMapper.toDto(posts);
    }

    /**
     * Update a posts.
     *
     * @param postsDTO the entity to save.
     * @return the persisted entity.
     */
    public PostsDTO update(PostsDTO postsDTO) {
        LOG.debug("Request to update Posts : {}", postsDTO);
        Posts posts = postsMapper.toEntity(postsDTO);
        posts = postsRepository.save(posts);
        return postsMapper.toDto(posts);
    }

    /**
     * Partially update a posts.
     *
     * @param postsDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PostsDTO> partialUpdate(PostsDTO postsDTO) {
        LOG.debug("Request to partially update Posts : {}", postsDTO);

        return postsRepository
            .findById(postsDTO.getId())
            .map(existingPosts -> {
                postsMapper.partialUpdate(existingPosts, postsDTO);

                return existingPosts;
            })
            .map(postsRepository::save)
            .map(postsMapper::toDto);
    }

    /**
     * Get one posts by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PostsDTO> findOne(Long id) {
        LOG.debug("Request to get Posts : {}", id);
        return postsRepository.findById(id).map(postsMapper::toDto);
    }

    /**
     * Delete the posts by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Posts : {}", id);
        postsRepository.deleteById(id);
    }
}
