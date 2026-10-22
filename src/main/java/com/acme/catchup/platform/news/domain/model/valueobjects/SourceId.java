package com.acme.catchup.platform.news.domain.model.valueobjects;

public record SourceId(String value) {
    private static final int MAX_LENGTH = 256;

    public SourceId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Source ID cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Source ID cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
