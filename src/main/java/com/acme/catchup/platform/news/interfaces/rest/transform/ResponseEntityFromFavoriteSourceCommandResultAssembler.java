package com.acme.catchup.platform.news.interfaces.rest.transform;

import com.acme.catchup.platform.news.application.commandservices.FavoriteSourceCommandFailure;
import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.shared.application.patterns.Result;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class ResponseEntityFromFavoriteSourceCommandResultAssembler {

    private static HttpStatus statusFromFailure(FavoriteSourceCommandFailure failure) {
        if (failure instanceof FavoriteSourceCommandFailure.Duplicate) return HttpStatus.CONFLICT;
        return HttpStatus.BAD_REQUEST;
    }

    private static String localizedMessage(MessageSource messageSource, String messageKey) {
        return messageSource.getMessage(messageKey, null, messageKey, LocaleContextHolder.getLocale());
    }

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
