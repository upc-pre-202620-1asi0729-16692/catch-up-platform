package com.acme.catchup.platform.news.domain.model.commands;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;

public record CreateFavoriteSourceCommand(NewsApiKey newsApiKey, SourceId sourceId) {
    public CreateFavoriteSourceCommand {
        if (newsApiKey == null) throw new IllegalArgumentException("News API key cannot be null");
        if (sourceId == null) throw new IllegalArgumentException("Source ID cannot be null");
    }
}
