package com.acme.catchup.platform.news.application.commandservices;

import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.news.domain.model.commands.CreateFavoriteSourceCommand;
import com.acme.catchup.platform.shared.application.patterns.Result;

public interface FavoriteSourceCommandService {
    Result<FavoriteSource, FavoriteSourceCommandFailure> handle(CreateFavoriteSourceCommand command);
}
