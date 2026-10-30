package com.acme.catchup.platform.news.domain.model.aggregates;

import com.acme.catchup.platform.news.domain.model.commands.CreateFavoriteSourceCommand;
import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;
import com.acme.catchup.platform.news.infrastructure.persistence.jpa.converters.NewsApiKeyAttributeConverter;
import com.acme.catchup.platform.news.infrastructure.persistence.jpa.converters.SourceIdAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Aggregate root representing a user's favorite news source mapping.
 * <p>
 * Links a {@link NewsApiKey} with a NewsAPI.org {@link SourceId}. Enforces uniqueness on the
 * pair {@code (newsApiKey, sourceId)} via a database unique constraint, and tracks lifecycle
 * auditing timestamps (created and updated dates) automatically.
 * </p>
 *
 * @since 1.0
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"newsApiKey", "sourceId"}, name = FavoriteSource.NEWS_API_KEY_SOURCE_ID_UNIQUE_CONSTRAINT)
})
public class FavoriteSource extends AbstractAggregateRoot<FavoriteSource> {
    /**
     * Name of the unique constraint enforcing uniqueness of (newsApiKey, sourceId) pairs.
     */
    public static final String NEWS_API_KEY_SOURCE_ID_UNIQUE_CONSTRAINT = "uk_favorite_source_news_api_key_source_id";

    /**
     * Unique numeric identifier for the favorite source entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The News API key value object associated with this favorite source.
     */
    @Column(nullable = false)
    @Convert(converter = NewsApiKeyAttributeConverter.class)
    private NewsApiKey newsApiKey;

    /**
     * The news source identifier value object marked as favorite.
     */
    @Column(nullable = false)
    @Convert(converter = SourceIdAttributeConverter.class)
    private SourceId sourceId;

    /**
     * Timestamp indicating when this favorite source was created.
     */
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    /**
     * Timestamp indicating when this favorite source was last modified.
     */
    @Column(nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Default protected constructor for JPA framework use.
     */
    protected FavoriteSource() {}

    /**
     * Constructs a new {@link FavoriteSource} aggregate from a creation command.
     *
     * @param command the {@link CreateFavoriteSourceCommand} containing the News API key and Source ID
     */
    public FavoriteSource(CreateFavoriteSourceCommand command) {
        this.newsApiKey = command.newsApiKey();
        this.sourceId = command.sourceId();
    }

}
