package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Reactions;
import com.mycompany.myapp.repository.ReactionsRepository;
import com.mycompany.myapp.service.dto.ReactionsDTO;
import com.mycompany.myapp.service.mapper.ReactionsMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Reactions}.
 */
@Service
@Transactional
public class ReactionsService {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionsService.class);

    private final ReactionsRepository reactionsRepository;

    private final ReactionsMapper reactionsMapper;

    public ReactionsService(ReactionsRepository reactionsRepository, ReactionsMapper reactionsMapper) {
        this.reactionsRepository = reactionsRepository;
        this.reactionsMapper = reactionsMapper;
    }

    /**
     * Save a reactions.
     *
     * @param reactionsDTO the entity to save.
     * @return the persisted entity.
     */
    public ReactionsDTO save(ReactionsDTO reactionsDTO) {
        LOG.debug("Request to save Reactions : {}", reactionsDTO);
        Reactions reactions = reactionsMapper.toEntity(reactionsDTO);
        reactions = reactionsRepository.save(reactions);
        return reactionsMapper.toDto(reactions);
    }

    /**
     * Update a reactions.
     *
     * @param reactionsDTO the entity to save.
     * @return the persisted entity.
     */
    public ReactionsDTO update(ReactionsDTO reactionsDTO) {
        LOG.debug("Request to update Reactions : {}", reactionsDTO);
        Reactions reactions = reactionsMapper.toEntity(reactionsDTO);
        reactions = reactionsRepository.save(reactions);
        return reactionsMapper.toDto(reactions);
    }

    /**
     * Partially update a reactions.
     *
     * @param reactionsDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReactionsDTO> partialUpdate(ReactionsDTO reactionsDTO) {
        LOG.debug("Request to partially update Reactions : {}", reactionsDTO);

        return reactionsRepository
            .findById(reactionsDTO.getId())
            .map(existingReactions -> {
                reactionsMapper.partialUpdate(existingReactions, reactionsDTO);

                return existingReactions;
            })
            .map(reactionsRepository::save)
            .map(reactionsMapper::toDto);
    }

    /**
     * Get one reactions by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReactionsDTO> findOne(Long id) {
        LOG.debug("Request to get Reactions : {}", id);
        return reactionsRepository.findById(id).map(reactionsMapper::toDto);
    }

    /**
     * Delete the reactions by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Reactions : {}", id);
        reactionsRepository.deleteById(id);
    }
}
