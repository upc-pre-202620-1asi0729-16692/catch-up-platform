package com.acme.catchup.platform.news.infrastructure.persistence.jpa.converters;

import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for transparent persistence mapping of {@link SourceId} domain value objects.
 * <p>
 * Converts between the {@link SourceId} value object in domain models and a VARCHAR column in the database.
 * </p>
 *
 * @since 1.0
 */
@Converter
public class SourceIdAttributeConverter implements AttributeConverter<SourceId, String> {
    /**
     * Converts a {@link SourceId} value object to its database column string representation.
     *
     * @param attribute the {@link SourceId} domain value object
     * @return the string value to be stored in the database, or {@code null} if attribute is null
     */
    @Override
    public String convertToDatabaseColumn(SourceId attribute) {
        return attribute == null ? null : attribute.value();
    }

    /**
     * Converts a database column string back to a {@link SourceId} domain value object.
     *
     * @param dbData the string value read from the database column
     * @return a new {@link SourceId} instance, or {@code null} if dbData is null
     */
    @Override
    public SourceId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new SourceId(dbData);
    }
}
