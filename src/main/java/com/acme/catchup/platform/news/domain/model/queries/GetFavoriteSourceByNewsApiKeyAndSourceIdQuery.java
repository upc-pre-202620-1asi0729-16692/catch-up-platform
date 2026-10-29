package com.acme.catchup.platform.news.domain.model.queries;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;

/**
 * Domain query record to retrieve a specific favorite source by the composite pair of
 * News API key and source identifier.
 *
 * @param newsApiKey the News API key value object
 * @param sourceId   the source identifier value object
 * @since 1.0
 */
public record GetFavoriteSourceByNewsApiKeyAndSourceIdQuery(NewsApiKey newsApiKey, SourceId sourceId ) {
    /**
     * Compact constructor validating that both query criteria value objects are non-null.
     *
     * @param newsApiKey the News API key value object
     * @param sourceId   the source ID value object
     * @throws IllegalArgumentException if {@code newsApiKey} or {@code sourceId} is null
     */
    public GetFavoriteSourceByNewsApiKeyAndSourceIdQuery {
        if (newsApiKey == null) {
            throw new IllegalArgumentException("NewsApiKey cannot be null");
        }
        if (sourceId == null) {
            throw new IllegalArgumentException("SourceId cannot be null");
        }
    }
}
