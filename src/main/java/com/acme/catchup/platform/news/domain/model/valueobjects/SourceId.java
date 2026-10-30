package com.acme.catchup.platform.news.domain.model.valueobjects;

/**
 * Domain value object representing a NewsAPI.org source identifier (e.g. "bbc-news").
 * <p>
 * Enforces business invariants verifying that the source identifier is neither null,
 * blank, nor exceeds the maximum permitted length of 256 characters.
 * </p>
 *
 * @param value the raw news source identifier string
 * @since 1.0
 */
public record SourceId(String value) {
    private static final int MAX_LENGTH = 256;

    /**
     * Compact constructor validating invariants for the source ID.
     *
     * @param value the raw source identifier string
     * @throws IllegalArgumentException if {@code value} is null, blank, or exceeds 256 characters
     */
    public SourceId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Source ID cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Source ID cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
