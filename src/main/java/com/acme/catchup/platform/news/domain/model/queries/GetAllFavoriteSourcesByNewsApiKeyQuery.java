package com.acme.catchup.platform.news.domain.model.queries;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;

/**
 * Domain query record to retrieve all favorite sources associated with a specific News API key.
 *
 * @param newsApiKey the News API key value object used to filter favorite sources
 * @since 1.0
 */
public record GetAllFavoriteSourcesByNewsApiKeyQuery(NewsApiKey newsApiKey) {
    /**
     * Compact constructor validating that the News API key is non-null.
     *
     * @param newsApiKey the News API key value object
     * @throws IllegalArgumentException if {@code newsApiKey} is null
     */
    public GetAllFavoriteSourcesByNewsApiKeyQuery {
        if (newsApiKey == null) {
            throw new IllegalArgumentException("NewsApiKey cannot be null");
        }
    }
}
