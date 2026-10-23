package com.acme.catchup.platform.news.domain.model.queries;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;

public record GetAllFavoriteSourcesByNewsApiKeyQuery(NewsApiKey newsApiKey) {
    public GetAllFavoriteSourcesByNewsApiKeyQuery {
        if (newsApiKey == null) {
            throw new IllegalArgumentException("NewsApiKey cannot be null");
        }
    }
}
