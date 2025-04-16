package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Comments;
import com.mycompany.myapp.domain.ReportedComments;
import com.mycompany.myapp.service.dto.CommentsDTO;
import com.mycompany.myapp.service.dto.ReportedCommentsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ReportedComments} and its DTO {@link ReportedCommentsDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReportedCommentsMapper extends EntityMapper<ReportedCommentsDTO, ReportedComments> {
    @Mapping(target = "comment", source = "comment", qualifiedByName = "commentsId")
    ReportedCommentsDTO toDto(ReportedComments s);

    @Named("commentsId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CommentsDTO toDtoCommentsId(Comments comments);
}
