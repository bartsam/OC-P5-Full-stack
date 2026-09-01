package com.openclassrooms.mddapi.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.openclassrooms.mddapi.dto.TopicItemResponse;
import com.openclassrooms.mddapi.dto.TopicOptionResponse;
import com.openclassrooms.mddapi.models.TopicEntity;

@Mapper(componentModel = "spring")
public interface TopicMapper {

  @Mapping(target = "isSubscribed", expression = "java(isSubscribed)")
  TopicItemResponse toItemDTO(TopicEntity topic, boolean isSubscribed);

  TopicOptionResponse toOptionDTO(TopicEntity topic);

}
