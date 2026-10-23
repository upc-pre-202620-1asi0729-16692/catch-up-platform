package com.acme.catchup.platform.news.interfaces.rest.transform;

import com.acme.catchup.platform.news.domain.model.valueobjects.NewsApiKey;
import com.acme.catchup.platform.news.domain.model.valueobjects.SourceId;

public class NewsValueObjectFromStringAssembler {
    public static NewsApiKey toNewsApiKeyFromString(String value) {
        return new NewsApiKey(value);
    }
    public static SourceId toSourceIdFromString(String value) {
        return new SourceId(value);
    }
}
