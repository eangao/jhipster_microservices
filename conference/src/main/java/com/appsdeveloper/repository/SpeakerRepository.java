package com.appsdeveloper.repository;

import com.appsdeveloper.domain.Speaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Speaker entity.
 */
@Repository
public interface SpeakerRepository extends JpaRepository<Speaker, Long> {
    @Query(
        value = "select distinct speaker from Speaker speaker left join fetch speaker.sessions",
        countQuery = "select count(distinct speaker) from Speaker speaker"
    )
    Page<Speaker> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct speaker from Speaker speaker left join fetch speaker.sessions")
    List<Speaker> findAllWithEagerRelationships();

    @Query("select speaker from Speaker speaker left join fetch speaker.sessions where speaker.id =:id")
    Optional<Speaker> findOneWithEagerRelationships(@Param("id") Long id);
}
