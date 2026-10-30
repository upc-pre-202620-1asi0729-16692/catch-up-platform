package com.acme.catchup.platform.news.interfaces.rest.transform;

import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Assembler responsible for translating query results and query error conditions into
 * Spring MVC {@link ResponseEntity} instances for REST HTTP responses.
 *
 * @since 1.0
 */
public class ResponseEntityFromFavoriteSourceQueryResultAssembler {
    private static ResponseEntity<ProblemDetail> problemDetail(
            HttpStatus status,
            MessageSource messageSource,
            String messageKey, Object... args) {
        var detail = messageSource.getMessage(messageKey, args, messageKey, LocaleContextHolder.getLocale());
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }

    /**
     * Translates a single {@link FavoriteSource} aggregate into an HTTP 200 OK response with {@link com.acme.catchup.platform.news.interfaces.rest.resources.FavoriteSourceResource}.
     *
     * @param favoriteSource the favorite source aggregate
     * @return a {@link ResponseEntity} containing the resource
     */
    public static ResponseEntity<?> toResponseEntityFromFavoriteSource(FavoriteSource favoriteSource) {
        return ResponseEntity.ok(FavoriteSourceResourceFromEntityAssembler.toResourceFromEntity(favoriteSource));
    }

    /**
     * Translates a list of {@link FavoriteSource} aggregates into an HTTP 200 OK response with a list of resources.
     *
     * @param favoriteSources the list of favorite source aggregates
     * @return a {@link ResponseEntity} containing the list of resources
     */
    public static ResponseEntity<?> toResponseEntityFromList(List<FavoriteSource> favoriteSources) {
        var favoriteSourceResources = favoriteSources.stream()
                .map(FavoriteSourceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(favoriteSourceResources);
    }

    /**
     * Creates an HTTP 400 Bad Request response with a localized {@link ProblemDetail}.
     *
     * @param messageSource the message source for localization
     * @param messageKey    the resource bundle message key
     * @param args          optional message arguments
     * @return a {@link ResponseEntity} with status 400 and problem detail body
     */
    public static ResponseEntity<ProblemDetail> badRequest(MessageSource messageSource, String messageKey, Object... args) {
        return problemDetail(HttpStatus.BAD_REQUEST, messageSource, messageKey, args);
    }

    /**
     * Creates an HTTP 404 Not Found response with a localized {@link ProblemDetail}.
     *
     * @param messageSource the message source for localization
     * @param messageKey    the resource bundle message key
     * @param args          optional message arguments
     * @return a {@link ResponseEntity} with status 404 and problem detail body
     */
    public static ResponseEntity<ProblemDetail> notFound(MessageSource messageSource, String messageKey, Object... args) {
        return problemDetail(HttpStatus.NOT_FOUND, messageSource, messageKey, args);
    }
}
