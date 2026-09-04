package com.openclassrooms.mddapi.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.models.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    List<PostEntity> findAllByOrderByCreatedAt(Sort sort);

}