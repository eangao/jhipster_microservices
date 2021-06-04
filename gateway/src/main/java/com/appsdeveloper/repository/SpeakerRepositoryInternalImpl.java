package com.appsdeveloper.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import com.appsdeveloper.domain.Session;
import com.appsdeveloper.domain.Speaker;
import com.appsdeveloper.repository.rowmapper.SpeakerRowMapper;
import com.appsdeveloper.service.EntityManager;
import com.appsdeveloper.service.EntityManager.LinkTable;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Speaker entity.
 */
@SuppressWarnings("unused")
class SpeakerRepositoryInternalImpl implements SpeakerRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final SpeakerRowMapper speakerMapper;

    private static final Table entityTable = Table.aliased("speaker", EntityManager.ENTITY_ALIAS);

    private static final EntityManager.LinkTable sessionsLink = new LinkTable("rel_speaker__sessions", "speaker_id", "sessions_id");

    public SpeakerRepositoryInternalImpl(R2dbcEntityTemplate template, EntityManager entityManager, SpeakerRowMapper speakerMapper) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.speakerMapper = speakerMapper;
    }

    @Override
    public Flux<Speaker> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Speaker> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Speaker> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = SpeakerSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);

        String select = entityManager.createSelect(selectFrom, Speaker.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(
                crit ->
                    new StringBuilder(select)
                        .append(" ")
                        .append("WHERE")
                        .append(" ")
                        .append(alias)
                        .append(".")
                        .append(crit.toString())
                        .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    @Override
    public Flux<Speaker> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Speaker> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    @Override
    public Mono<Speaker> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Speaker> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Speaker> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Speaker process(Row row, RowMetadata metadata) {
        Speaker entity = speakerMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends Speaker> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends Speaker> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity).flatMap(savedEntity -> updateRelations(savedEntity));
        } else {
            return update(entity)
                .map(
                    numberOfUpdates -> {
                        if (numberOfUpdates.intValue() <= 0) {
                            throw new IllegalStateException("Unable to update Speaker with id = " + entity.getId());
                        }
                        return entity;
                    }
                )
                .then(updateRelations(entity));
        }
    }

    @Override
    public Mono<Integer> update(Speaker entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }

    @Override
    public Mono<Void> deleteById(Long entityId) {
        return deleteRelations(entityId)
            .then(r2dbcEntityTemplate.delete(Speaker.class).matching(query(where("id").is(entityId))).all().then());
    }

    protected <S extends Speaker> Mono<S> updateRelations(S entity) {
        Mono<Void> result = entityManager
            .updateLinkTable(sessionsLink, entity.getId(), entity.getSessions().stream().map(Session::getId))
            .then();
        return result.thenReturn(entity);
    }

    protected Mono<Void> deleteRelations(Long entityId) {
        return entityManager.deleteFromLinkTable(sessionsLink, entityId);
    }
}

class SpeakerSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("first_name", table, columnPrefix + "_first_name"));
        columns.add(Column.aliased("last_name", table, columnPrefix + "_last_name"));
        columns.add(Column.aliased("email", table, columnPrefix + "_email"));
        columns.add(Column.aliased("twitter", table, columnPrefix + "_twitter"));
        columns.add(Column.aliased("bio", table, columnPrefix + "_bio"));

        return columns;
    }
}
