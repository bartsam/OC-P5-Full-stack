package com.openclassrooms.mddapi.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.openclassrooms.mddapi.dto.CommentResponse;
import com.openclassrooms.mddapi.models.CommentEntity;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "author.username", target = "author")
    CommentResponse toResponse(CommentEntity comment);

}
