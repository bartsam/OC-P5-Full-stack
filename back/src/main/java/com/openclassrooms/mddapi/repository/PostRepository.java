package com.openclassrooms.mddapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.models.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}