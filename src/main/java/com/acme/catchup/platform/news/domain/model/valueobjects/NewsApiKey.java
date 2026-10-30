package com.acme.catchup.platform.news.domain.model.valueobjects;

/**
 * Domain value object representing a NewsAPI.org API key.
 * <p>
 * Encapsulates validation rules ensuring the key is neither null, blank, nor exceeds
 * the maximum allowed length of 256 characters. Also provides log-safe masking to protect
 * sensitive key credentials from being leaked in application logs.
 * </p>
 *
 * @param value the raw API key string
 * @since 1.0
 */
public record NewsApiKey(String value) {
    private static final int MAX_LENGTH = 256;

    /**
     * Compact constructor validating invariants for the News API key.
     *
     * @param value the raw API key string
     * @throws IllegalArgumentException if {@code value} is null, blank, or exceeds 256 characters
     */
    public NewsApiKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("News API key cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("News API key cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Returns a masked representation of the API key suitable for diagnostic logging
     * without exposing sensitive credentials.
     * <p>
     * Replaces leading characters with asterisks, preserving at most the last 4 characters.
     * If the key has 4 or fewer characters, returns {@code "****"}.
     * </p>
     *
     * @return a masked string representation of the API key
     */
    public String maskedForLogging() {
        if (value.length() <= 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }
}
