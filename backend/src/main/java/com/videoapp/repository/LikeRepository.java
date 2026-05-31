package com.videoapp.repository;

import com.videoapp.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndVideoId(Long userId, Long videoId);
    Page<Like> findByVideoId(Long videoId, Pageable pageable);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.video.id = :videoId AND l.isLike = :isLike")
    Long countByVideoIdAndIsLike(@Param("videoId") Long videoId, @Param("isLike") Boolean isLike);
    void deleteByUserIdAndVideoId(Long userId, Long videoId);
}
