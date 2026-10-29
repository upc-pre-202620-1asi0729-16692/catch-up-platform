package com.acme.catchup.platform.news.domain.model.queries;

/**
 * Domain query record to retrieve a favorite source by its unique persistent identifier.
 *
 * @param id the unique numeric identifier of the favorite source
 * @since 1.0
 */
public record GetFavoriteSourceByIdQuery(Long id) {
    /**
     * Compact constructor validating that the identifier is positive and non-null.
     *
     * @param id the unique numeric identifier
     * @throws IllegalArgumentException if {@code id} is null or less than or equal to zero
     */
    public GetFavoriteSourceByIdQuery {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("favorite.source.error.id.invalid");
        }
    }
}
