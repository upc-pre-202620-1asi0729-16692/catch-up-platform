package com.acme.catchup.platform.news.interfaces.rest.transform;

import com.acme.catchup.platform.news.domain.model.commands.CreateFavoriteSourceCommand;
import com.acme.catchup.platform.news.interfaces.rest.resources.CreateFavoriteSourceResource;

/**
 * Assembler responsible for transforming inbound {@link CreateFavoriteSourceResource} REST payloads
 * into internal domain {@link CreateFavoriteSourceCommand} objects.
 *
 * @since 1.0
 */
public class CreateFavoriteSourceCommandFromResourceAssembler {
    /**
     * Translates a {@link CreateFavoriteSourceResource} request payload into a {@link CreateFavoriteSourceCommand}.
     *
     * @param resource the incoming REST request resource
     * @return the corresponding domain {@link CreateFavoriteSourceCommand}
     */
    public static CreateFavoriteSourceCommand toCommandFromResource(CreateFavoriteSourceResource resource) {
        return new CreateFavoriteSourceCommand(
                NewsValueObjectFromStringAssembler.toNewsApiKeyFromString(resource.newsApiKey()),
                NewsValueObjectFromStringAssembler.toSourceIdFromString(resource.sourceId()));
    }
}
