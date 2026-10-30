package com.acme.catchup.platform.news.application.commandservices;

import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.news.domain.model.commands.CreateFavoriteSourceCommand;
import com.acme.catchup.platform.shared.application.patterns.Result;

/**
 * Application service contract for executing favorite source domain commands.
 * <p>
 * Defines operations that alter the state of the favorite sources bounded context,
 * returning a functional {@link Result} encapsulating either the created {@link FavoriteSource}
 * aggregate root or a {@link FavoriteSourceCommandFailure}.
 * </p>
 *
 * @since 1.0
 */
public interface FavoriteSourceCommandService {
    /**
     * Handles the creation of a new favorite source.
     *
     * @param command the {@link CreateFavoriteSourceCommand} containing source and key details
     * @return a {@link Result} holding either the successfully created {@link FavoriteSource} or a failure
     */
    Result<FavoriteSource, FavoriteSourceCommandFailure> handle(CreateFavoriteSourceCommand command);
}
