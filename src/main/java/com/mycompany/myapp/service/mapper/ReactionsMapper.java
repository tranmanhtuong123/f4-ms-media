package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Comments;
import com.mycompany.myapp.domain.Posts;
import com.mycompany.myapp.domain.Reactions;
import com.mycompany.myapp.service.dto.CommentsDTO;
import com.mycompany.myapp.service.dto.PostsDTO;
import com.mycompany.myapp.service.dto.ReactionsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Reactions} and its DTO {@link ReactionsDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReactionsMapper extends EntityMapper<ReactionsDTO, Reactions> {
    @Mapping(target = "post", source = "post", qualifiedByName = "postsId")
    @Mapping(target = "comment", source = "comment", qualifiedByName = "commentsId")
    ReactionsDTO toDto(Reactions s);

    @Named("postsId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PostsDTO toDtoPostsId(Posts posts);

    @Named("commentsId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CommentsDTO toDtoCommentsId(Comments comments);
}
