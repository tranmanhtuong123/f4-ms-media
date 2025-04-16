package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Comments;
import com.mycompany.myapp.domain.Posts;
import com.mycompany.myapp.service.dto.CommentsDTO;
import com.mycompany.myapp.service.dto.PostsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Comments} and its DTO {@link CommentsDTO}.
 */
@Mapper(componentModel = "spring")
public interface CommentsMapper extends EntityMapper<CommentsDTO, Comments> {
    @Mapping(target = "post", source = "post", qualifiedByName = "postsId")
    @Mapping(target = "parentComment", source = "parentComment", qualifiedByName = "commentsId")
    CommentsDTO toDto(Comments s);

    @Named("postsId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PostsDTO toDtoPostsId(Posts posts);

    @Named("commentsId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CommentsDTO toDtoCommentsId(Comments comments);
}
