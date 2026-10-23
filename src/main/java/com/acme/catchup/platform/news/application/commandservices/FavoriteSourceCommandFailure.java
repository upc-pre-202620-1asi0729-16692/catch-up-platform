package com.acme.catchup.platform.news.application.commandservices;

public sealed interface FavoriteSourceCommandFailure permits FavoriteSourceCommandFailure.Duplicate {
    String messageKey();

    record Duplicate() implements FavoriteSourceCommandFailure {
        @Override
        public String messageKey() {
            return "favorite.source.error.duplicate";
        }
    }
}
