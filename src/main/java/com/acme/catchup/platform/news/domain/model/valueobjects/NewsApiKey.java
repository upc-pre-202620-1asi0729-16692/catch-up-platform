package com.acme.catchup.platform.news.domain.model.valueobjects;

public record NewsApiKey(String value) {
    private static final int MAX_LENGTH = 256;

    public NewsApiKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("News API key cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("News API key cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public String maskedForLogging() {
        if (value.length() <= 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }
}
