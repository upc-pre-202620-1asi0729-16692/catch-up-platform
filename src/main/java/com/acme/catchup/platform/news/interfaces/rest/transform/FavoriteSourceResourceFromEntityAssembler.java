package com.acme.catchup.platform.news.interfaces.rest.transform;

import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.news.interfaces.rest.resources.FavoriteSourceResource;

/**
 * Assembler responsible for transforming domain {@link FavoriteSource} aggregate entities
 * into outbound {@link FavoriteSourceResource} REST response representations.
 *
 * @since 1.0
 */
public class FavoriteSourceResourceFromEntityAssembler {
    /**
     * Converts a {@link FavoriteSource} entity to a {@link FavoriteSourceResource}.
     *
     * @param entity the favorite source domain aggregate entity
     * @return the outbound REST resource representation
     */
    public static FavoriteSourceResource toResourceFromEntity(FavoriteSource entity) {
        return new FavoriteSourceResource(
                entity.getId(),
                entity.getNewsApiKey().value(),
                entity.getSourceId().value());
    }
}
