package com.acme.catchup.platform.news.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response resource record representing a favorite source in the REST API.
 *
 * @param id         the unique numeric identifier of the favorite source
 * @param newsApiKey the News API key associated with the favorite source
 * @param sourceId   the news source identifier marked as favorite
 * @since 1.0
 */
@Schema(description = "Response resource representing a favorite source.")
public record FavoriteSourceResource(
        @Schema(description = "Persistent identifier", example = "1")
        Long id,
        @Schema(description = "News API key associated with the favorite source", example = "news-api-key-123")
        String newsApiKey,
        @Schema(description = "Source identifier marked as favorite", example = "bbc-news")
        String sourceId) {}
