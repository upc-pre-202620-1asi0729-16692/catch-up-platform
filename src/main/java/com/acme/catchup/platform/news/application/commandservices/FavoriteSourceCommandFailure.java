package com.acme.catchup.platform.news.application.commandservices;

/**
 * Domain-specific failure representation for favorite source command operations.
 * <p>
 * Implemented as a sealed interface defining failure outcomes with their corresponding
 * localization message keys.
 * </p>
 *
 * @since 1.0
 */
public sealed interface FavoriteSourceCommandFailure permits FavoriteSourceCommandFailure.Duplicate {
    /**
     * Returns the message key used for localization of this failure.
     *
     * @return the resource bundle message key string
     */
    String messageKey();

    /**
     * Failure outcome indicating that a favorite source with the specified News API key
     * and source ID already exists.
     *
     * @since 1.0
     */
    record Duplicate() implements FavoriteSourceCommandFailure {
        /**
         * Returns the localization message key for duplicate favorite source conflicts.
         *
         * @return {@code "favorite.source.error.duplicate"}
         */
        @Override
        public String messageKey() {
            return "favorite.source.error.duplicate";
        }
    }
}
