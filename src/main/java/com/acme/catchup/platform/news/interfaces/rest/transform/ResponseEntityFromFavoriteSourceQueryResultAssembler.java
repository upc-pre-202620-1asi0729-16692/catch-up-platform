package com.acme.catchup.platform.news.interfaces.rest.transform;

import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ResponseEntityFromFavoriteSourceQueryResultAssembler {
    private static ResponseEntity<ProblemDetail> problemDetail(
            HttpStatus status,
            MessageSource messageSource,
            String messageKey, Object... args) {
        var detail = messageSource.getMessage(messageKey, args, messageKey, LocaleContextHolder.getLocale());
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }

    public static ResponseEntity<?> toResponseEntityFromFavoriteSource(FavoriteSource favoriteSource) {
        return ResponseEntity.ok(FavoriteSourceResourceFromEntityAssembler.toResourceFromEntity(favoriteSource));
    }

    public static ResponseEntity<?> toResponseEntityFromList(List<FavoriteSource> favoriteSources) {
        var favoriteSourceResources = favoriteSources.stream()
                .map(FavoriteSourceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(favoriteSourceResources);
    }

    public static ResponseEntity<ProblemDetail> badRequest(MessageSource messageSource, String messageKey, Object... args) {
        return problemDetail(HttpStatus.BAD_REQUEST, messageSource, messageKey, args);
    }

    public static ResponseEntity<ProblemDetail> notFound(MessageSource messageSource, String messageKey, Object... args) {
        return problemDetail(HttpStatus.NOT_FOUND, messageSource, messageKey, args);
    }
}
