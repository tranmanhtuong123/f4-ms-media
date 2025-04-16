package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Posts;
import com.mycompany.myapp.service.dto.PostsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Posts} and its DTO {@link PostsDTO}.
 */
@Mapper(componentModel = "spring")
public interface PostsMapper extends EntityMapper<PostsDTO, Posts> {}
