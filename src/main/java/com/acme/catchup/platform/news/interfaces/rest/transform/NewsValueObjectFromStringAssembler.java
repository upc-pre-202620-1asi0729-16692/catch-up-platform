package com.acme.catchup.platform.news.interfaces.rest.transform;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;

/**
 * Assembler utility for creating domain value objects ({@link NewsApiKey} and {@link SourceId})
 * from raw string parameters supplied across the interface boundary.
 *
 * @since 1.0
 */
public class NewsValueObjectFromStringAssembler {
    /**
     * Instantiates a {@link NewsApiKey} value object from a raw string value.
     *
     * @param value the raw string API key
     * @return a validated {@link NewsApiKey} value object
     */
    public static NewsApiKey toNewsApiKeyFromString(String value) {
        return new NewsApiKey(value);
    }

    /**
     * Instantiates a {@link SourceId} value object from a raw string value.
     *
     * @param value the raw string source identifier
     * @return a validated {@link SourceId} value object
     */
    public static SourceId toSourceIdFromString(String value) {
        return new SourceId(value);
    }
}
