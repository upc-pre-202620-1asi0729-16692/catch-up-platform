package com.acme.catchup.platform.news.infrastructure.persistence.jpa.converters;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import jakarta.persistence.Converter;
import jakarta.persistence.AttributeConverter;

@Converter
public class NewsApiKeyAttributeConverter implements AttributeConverter<NewsApiKey, String> {
    @Override
    public String convertToDatabaseColumn(NewsApiKey attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public NewsApiKey convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new NewsApiKey(dbData);
    }
}
