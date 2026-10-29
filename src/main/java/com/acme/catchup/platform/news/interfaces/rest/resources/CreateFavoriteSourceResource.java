package com.acme.catchup.platform.news.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request resource record used to create a new favorite source via the REST API.
 * <p>
 * Carries input validation constraints (non-blank, size, allowed character pattern)
 * for the News API key and source identifier.
 * </p>
 *
 * @param newsApiKey News API key associated with the favorite source
 * @param sourceId   source identifier to mark as favorite
 * @since 1.0
 */
@Schema(description = "Request resource used to create a favorite source.")
public record CreateFavoriteSourceResource(
        @Schema(
                description = "News API key associated with the favorite source",
                example = "news-api-key-123",
                maxLength = 256,
                pattern = "^[a-zA-z0-9-_:-]+$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message="{favorite.source.error.newsApiKey.notBlank}")
        @Size(max = 256, message="{favorite.source.error.newsApiKey.size}")
        @Pattern(regexp = "^[a-zA-z0-9-_:-]+$", message="{favorite.source.error.newsApiKey.pattern}")
        String newsApiKey,
        @Schema(
                description= "Source identifier to mark as favorite",
                example = "bbc-news",
                maxLength = 256,
                pattern = "^[a-zA-z0-9-_:-]+$",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message="{favorite.source.error.sourceId.notBlank}")
        @Size(max = 256, message="{favorite.source.error.sourceId.size}")
        @Pattern(regexp = "^[a-zA-z0-9-_:-]+$", message="{favorite.source.error.sourceId.pattern}")
        String sourceId) {}
