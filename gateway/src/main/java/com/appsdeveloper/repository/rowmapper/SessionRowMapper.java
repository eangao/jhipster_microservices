package com.appsdeveloper.repository.rowmapper;

import com.appsdeveloper.domain.Session;
import com.appsdeveloper.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Session}, with proper type conversions.
 */
@Service
public class SessionRowMapper implements BiFunction<Row, String, Session> {

    private final ColumnConverter converter;

    public SessionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Session} stored in the database.
     */
    @Override
    public Session apply(Row row, String prefix) {
        Session entity = new Session();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setTitle(converter.fromRow(row, prefix + "_title", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setStartDateTime(converter.fromRow(row, prefix + "_start_date_time", ZonedDateTime.class));
        entity.setEndDateTime(converter.fromRow(row, prefix + "_end_date_time", ZonedDateTime.class));
        return entity;
    }
}
