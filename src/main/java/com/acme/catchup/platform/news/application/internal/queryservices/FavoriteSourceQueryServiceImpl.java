package com.acme.catchup.platform.news.application.internal.queryservices;

import com.acme.catchup.platform.news.application.queryservices.FavoriteSourceQueryService;
import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.news.domain.model.queries.GetAllFavoriteSourcesByNewsApiKeyQuery;
import com.acme.catchup.platform.news.domain.model.queries.GetFavoriteSourceByIdQuery;
import com.acme.catchup.platform.news.domain.model.queries.GetFavoriteSourceByNewsApiKeyAndSourceIdQuery;
import com.acme.catchup.platform.news.infrastructure.persistence.jpa.repositories.FavoriteSourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FavoriteSourceQueryServiceImpl implements FavoriteSourceQueryService {
    private final FavoriteSourceRepository favoriteSourceRepository;

    public FavoriteSourceQueryServiceImpl(FavoriteSourceRepository favoriteSourceRepository) {
        this.favoriteSourceRepository = favoriteSourceRepository;
    }

    @Override
    public List<FavoriteSource> handle(GetAllFavoriteSourcesByNewsApiKeyQuery query) {
        log.debug("Querying all favorite sources for newsApiKey: {}", query.newsApiKey().maskedForLogging());
        var results = favoriteSourceRepository.findAllByNewsApiKey(query.newsApiKey());
        log.debug("Found {} favorite sources for newsApiKey: {}", results.size(), query.newsApiKey().maskedForLogging());
        return results;
    }

    @Override
    public Optional<FavoriteSource> handle(GetFavoriteSourceByIdQuery query) {
        log.debug("Querying favorite source by id: {}", query.id());
        var result = favoriteSourceRepository.findById(query.id());
        if (result.isEmpty()) log.debug("No favorite source found for id: {}", query.id());
        return result;
    }

    @Override
    public Optional<FavoriteSource> handle(GetFavoriteSourceByNewsApiKeyAndSourceIdQuery query) {
        log.debug("Querying favorite source by newsApiKey: {} and sourceId: {}", query.newsApiKey().maskedForLogging(), query.sourceId());
        var result = favoriteSourceRepository.findByNewsApiKeyAndSourceId(query.newsApiKey(), query.sourceId());
        if (result.isEmpty()) log.debug("No favorite source found for newsApiKey: {} and sourceId: {}", query.newsApiKey().maskedForLogging(), query.sourceId());
        return result;
    }
}
