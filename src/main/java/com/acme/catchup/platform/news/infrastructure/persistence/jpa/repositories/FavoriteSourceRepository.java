package com.acme.catchup.platform.news.infrastructure.persistence.jpa.repositories;

import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for managing {@link FavoriteSource} aggregate persistence.
 * <p>
 * Provides persistence queries for retrieving, counting, and verifying favorite source mappings
 * by News API key and source identifier.
 * </p>
 *
 * @since 1.0
 */
@Repository
public interface FavoriteSourceRepository extends JpaRepository<FavoriteSource, Long> {
    /**
     * Finds all favorite sources associated with the given News API key.
     *
     * @param newsApiKey the News API key value object
     * @return a list of matching favorite source aggregates
     */
    List<FavoriteSource> findAllByNewsApiKey(NewsApiKey newsApiKey);

    /**
     * Checks if a favorite source already exists for the given News API key and source ID.
     *
     * @param newsApiKey the News API key value object
     * @param sourceId   the source ID value object
     * @return {@code true} if a record exists, {@code false} otherwise
     */
    boolean existsByNewsApiKeyAndSourceId(NewsApiKey newsApiKey, SourceId sourceId);

    /**
     * Finds a favorite source matching both the News API key and source ID.
     *
     * @param newsApiKey the News API key value object
     * @param sourceId   the source ID value object
     * @return an optional containing the matching favorite source aggregate if present
     */
    Optional<FavoriteSource> findByNewsApiKeyAndSourceId(NewsApiKey newsApiKey, SourceId sourceId);
}

