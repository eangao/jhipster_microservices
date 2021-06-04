package com.appsdeveloper.repository;

import com.appsdeveloper.domain.Session;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Session entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SessionRepository extends R2dbcRepository<Session, Long>, SessionRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<Session> findAll();

    @Override
    Mono<Session> findById(Long id);

    @Override
    <S extends Session> Mono<S> save(S entity);
}

interface SessionRepositoryInternal {
    <S extends Session> Mono<S> insert(S entity);
    <S extends Session> Mono<S> save(S entity);
    Mono<Integer> update(Session entity);

    Flux<Session> findAll();
    Mono<Session> findById(Long id);
    Flux<Session> findAllBy(Pageable pageable);
    Flux<Session> findAllBy(Pageable pageable, Criteria criteria);
}
