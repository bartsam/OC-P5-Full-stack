package com.openclassrooms.mddapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.openclassrooms.mddapi.models.TopicEntity;

@Repository
public interface TopicRepository extends JpaRepository<TopicEntity, Long> {
}
