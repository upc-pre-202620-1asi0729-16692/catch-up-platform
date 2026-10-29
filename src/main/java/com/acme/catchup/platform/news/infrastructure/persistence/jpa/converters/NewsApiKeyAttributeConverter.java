package com.acme.catchup.platform.news.infrastructure.persistence.jpa.converters;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;

/**
 * JPA attribute converter for transparent persistence mapping of {@link NewsApiKey} domain value objects.
 * <p>
 * Converts between the {@link NewsApiKey} value object in domain models and a VARCHAR column in the database.
 * </p>
 *
 * @since 1.0
 */
@Converter
public class NewsApiKeyAttributeConverter implements AttributeConverter<NewsApiKey, String> {
    /**
     * Converts a {@link NewsApiKey} value object to its database column string representation.
     *
     * @param attribute the {@link NewsApiKey} domain value object
     * @return the string value to be stored in the database, or {@code null} if attribute is null
     */
    @Override
    public String convertToDatabaseColumn(NewsApiKey attribute) {
        return attribute == null ? null : attribute.value();
    }

    /**
     * Converts a database column string back to a {@link NewsApiKey} domain value object.
     *
     * @param dbData the string value read from the database column
     * @return a new {@link NewsApiKey} instance, or {@code null} if dbData is null
     */
    @Override
    public NewsApiKey convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new NewsApiKey(dbData);
    }
}
