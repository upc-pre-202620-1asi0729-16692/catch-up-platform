package com.acme.catchup.platform.news.infrastructure.persistence.jpa.repositories;

import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteSourceRepository extends JpaRepository<FavoriteSource, Long> {
    List<FavoriteSource> findAllByNewsApiKey(NewsApiKey newsApiKey);
    boolean existsByNewsApiKeyAndSourceId(NewsApiKey newsApiKey, SourceId sourceId);
    Optional<FavoriteSource> findByNewsApiKeyAndSourceId(NewsApiKey newsApiKey, SourceId sourceId);
}

