package com.appsdeveloper.repository.rowmapper;

import com.appsdeveloper.domain.Speaker;
import com.appsdeveloper.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Speaker}, with proper type conversions.
 */
@Service
public class SpeakerRowMapper implements BiFunction<Row, String, Speaker> {

    private final ColumnConverter converter;

    public SpeakerRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Speaker} stored in the database.
     */
    @Override
    public Speaker apply(Row row, String prefix) {
        Speaker entity = new Speaker();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setFirstName(converter.fromRow(row, prefix + "_first_name", String.class));
        entity.setLastName(converter.fromRow(row, prefix + "_last_name", String.class));
        entity.setEmail(converter.fromRow(row, prefix + "_email", String.class));
        entity.setTwitter(converter.fromRow(row, prefix + "_twitter", String.class));
        entity.setBio(converter.fromRow(row, prefix + "_bio", String.class));
        return entity;
    }
}
