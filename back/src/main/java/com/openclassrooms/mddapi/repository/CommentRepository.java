package com.openclassrooms.mddapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.models.CommentEntity;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

}
