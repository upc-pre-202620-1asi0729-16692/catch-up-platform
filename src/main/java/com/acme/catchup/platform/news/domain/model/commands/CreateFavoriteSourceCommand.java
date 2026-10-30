package com.acme.catchup.platform.news.domain.model.commands;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;

/**
 * Command domain record representing the intent to create a new favorite news source.
 * <p>
 * Encapsulates the required domain value objects {@link NewsApiKey} and {@link SourceId},
 * verifying that neither component is null upon instantiation.
 * </p>
 *
 * @param newsApiKey the News API key associated with the favorite source
 * @param sourceId   the NewsAPI.org source identifier
 * @since 1.0
 */
public record CreateFavoriteSourceCommand(NewsApiKey newsApiKey, SourceId sourceId) {
    /**
     * Compact constructor validating that required value objects are non-null.
     *
     * @param newsApiKey the News API key value object
     * @param sourceId   the source ID value object
     * @throws IllegalArgumentException if {@code newsApiKey} or {@code sourceId} is null
     */
    public CreateFavoriteSourceCommand {
        if (newsApiKey == null) throw new IllegalArgumentException("News API key cannot be null");
        if (sourceId == null) throw new IllegalArgumentException("Source ID cannot be null");
    }
}
