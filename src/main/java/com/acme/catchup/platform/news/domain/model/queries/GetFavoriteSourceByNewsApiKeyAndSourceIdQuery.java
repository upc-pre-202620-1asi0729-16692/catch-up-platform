package com.acme.catchup.platform.news.domain.model.queries;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;

public record GetFavoriteSourceByNewsApiKeyAndSourceIdQuery(NewsApiKey newsApiKey, SourceId sourceId ) {
    public GetFavoriteSourceByNewsApiKeyAndSourceIdQuery {
        if (newsApiKey == null) {
            throw new IllegalArgumentException("NewsApiKey cannot be null");
        }
        if (sourceId == null) {
            throw new IllegalArgumentException("SourceId cannot be null");
        }
    }
}
