package com.openclassrooms.mddapi.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.openclassrooms.mddapi.dto.PostDetailResponse;
import com.openclassrooms.mddapi.dto.PostItemResponse;
import com.openclassrooms.mddapi.models.PostEntity;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "content", source = "content", qualifiedByName = "truncateContent")
    PostItemResponse toItemResponse(PostEntity post);

    @Mapping(source = "author.username", target = "author", defaultValue = "anonymous")
    @Mapping(source = "topic.name", target = "topic", defaultValue = "unknown")
    PostDetailResponse toDetailResponse(PostEntity post);

    @Named("truncateContent")
    default String truncateContent(String content) {
        if (content == null) {
            return null;
        }
        int max = 150;
        return content.length() <= max ? content : content.substring(0, max) + "…";
    }
}