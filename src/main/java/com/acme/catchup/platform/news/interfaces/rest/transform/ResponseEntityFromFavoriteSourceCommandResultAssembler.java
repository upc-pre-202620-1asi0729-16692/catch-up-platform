package com.acme.catchup.platform.news.interfaces.rest.transform;

import com.acme.catchup.platform.news.application.commandservices.FavoriteSourceCommandFailure;
import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.shared.application.patterns.Result;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Assembler responsible for translating application command {@link Result} objects into
 * Spring MVC {@link ResponseEntity} instances for REST HTTP responses.
 * <p>
 * Handles mapping successful aggregate outcomes to HTTP 201 Created and failures (such as duplicates)
 * to localized {@link ProblemDetail} responses (e.g. HTTP 409 Conflict).
 * </p>
 *
 * @since 1.0
 */
public class ResponseEntityFromFavoriteSourceCommandResultAssembler {

    private static HttpStatus statusFromFailure(FavoriteSourceCommandFailure failure) {
        if (failure instanceof FavoriteSourceCommandFailure.Duplicate) return HttpStatus.CONFLICT;
        return HttpStatus.BAD_REQUEST;
    }

    private static String localizedMessage(MessageSource messageSource, String messageKey) {
        return messageSource.getMessage(messageKey, null, messageKey, LocaleContextHolder.getLocale());
    }

    /**
     * Maps a favorite source command {@link Result} into an appropriate {@link ResponseEntity}.
     *
     * @param result        the command execution result
     * @param messageSource the {@link MessageSource} used for error message localization
     * @return a {@link ResponseEntity} with status 201 Created on success, or an error status with a {@link ProblemDetail}
     */
    public static ResponseEntity<?> toResponseEntityFromResult(
            Result<FavoriteSource, FavoriteSourceCommandFailure> result,
            MessageSource messageSource) {
        return result.fold(
                source -> new ResponseEntity<>(
                        FavoriteSourceResourceFromEntityAssembler.toResourceFromEntity(source),
                        HttpStatus.CREATED),
                failure -> {
                    var status = statusFromFailure(failure);
                    return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(
                            status,
                            localizedMessage(messageSource, failure.messageKey())));
                });
    }
}
