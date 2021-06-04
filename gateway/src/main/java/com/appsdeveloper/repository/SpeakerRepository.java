package com.appsdeveloper.repository;

import com.appsdeveloper.domain.Speaker;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Speaker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SpeakerRepository extends R2dbcRepository<Speaker, Long>, SpeakerRepositoryInternal {
    @Override
    Mono<Speaker> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Speaker> findAllWithEagerRelationships();

    @Override
    Flux<Speaker> findAllWithEagerRelationships(Pageable page);

    @Override
    Mono<Void> deleteById(Long id);

    @Query(
        "SELECT entity.* FROM speaker entity JOIN rel_speaker__sessions joinTable ON entity.id = joinTable.speaker_id WHERE joinTable.sessions_id = :id"
    )
    Flux<Speaker> findBySessions(Long id);

    // just to avoid having unambigous methods
    @Override
    Flux<Speaker> findAll();

    @Override
    Mono<Speaker> findById(Long id);

    @Override
    <S extends Speaker> Mono<S> save(S entity);
}

interface SpeakerRepositoryInternal {
    <S extends Speaker> Mono<S> insert(S entity);
    <S extends Speaker> Mono<S> save(S entity);
    Mono<Integer> update(Speaker entity);

    Flux<Speaker> findAll();
    Mono<Speaker> findById(Long id);
    Flux<Speaker> findAllBy(Pageable pageable);
    Flux<Speaker> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Speaker> findOneWithEagerRelationships(Long id);

    Flux<Speaker> findAllWithEagerRelationships();

    Flux<Speaker> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
