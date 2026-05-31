package com.videoapp.repository;

import com.videoapp.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByVideoId(Long videoId, Pageable pageable);
    void deleteByIdAndUserId(Long commentId, Long userId);
}
