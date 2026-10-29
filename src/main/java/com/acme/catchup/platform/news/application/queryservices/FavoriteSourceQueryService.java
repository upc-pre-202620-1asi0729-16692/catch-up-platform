package com.acme.catchup.platform.news.application.queryservices;

import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.news.domain.model.queries.GetAllFavoriteSourcesByNewsApiKeyQuery;
import com.acme.catchup.platform.news.domain.model.queries.GetFavoriteSourceByIdQuery;
import com.acme.catchup.platform.news.domain.model.queries.GetFavoriteSourceByNewsApiKeyAndSourceIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for querying favorite sources.
 * <p>
 * Exposes query operations for fetching favorite sources by News API key,
 * by unique identifier, or by composite News API key and source identifier.
 * </p>
 *
 * @since 1.0
 */
public interface FavoriteSourceQueryService {
    /**
     * Retrieves all favorite sources associated with the given News API key.
     *
     * @param query the {@link GetAllFavoriteSourcesByNewsApiKeyQuery} containing the key
     * @return a list of matching {@link FavoriteSource} aggregates, or empty list if none found
     */
    List<FavoriteSource> handle(GetAllFavoriteSourcesByNewsApiKeyQuery query);

    /**
     * Retrieves a favorite source by its unique numeric identifier.
     *
     * @param query the {@link GetFavoriteSourceByIdQuery} containing the target ID
     * @return an {@link Optional} containing the {@link FavoriteSource} if found, or empty otherwise
     */
    Optional<FavoriteSource> handle(GetFavoriteSourceByIdQuery query);

    /**
     * Retrieves a favorite source by its News API key and source identifier.
     *
     * @param query the {@link GetFavoriteSourceByNewsApiKeyAndSourceIdQuery} containing search criteria
     * @return an {@link Optional} containing the {@link FavoriteSource} if found, or empty otherwise
     */
    Optional<FavoriteSource> handle(GetFavoriteSourceByNewsApiKeyAndSourceIdQuery query);
}
