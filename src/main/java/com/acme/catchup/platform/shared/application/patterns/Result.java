package com.acme.catchup.platform.shared.application.patterns;

import java.util.Optional;
import java.util.function.Function;

/**
 * Generic functional result container representing either a successful outcome containing
 * a value of type {@code T} or a failure outcome containing an error of type {@code E}.
 * <p>
 * Implemented as a sealed interface with two records: {@link Result.Success} and {@link Result.Failure}.
 * Enables explicit domain/application-level error handling without relying on runtime exceptions.
 * </p>
 *
 * @param <T> the type of the successful result value
 * @param <E> the type of the failure error
 * @since 1.0
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    /**
     * Successful outcome record holding a value of type {@code T}.
     *
     * @param value the successful computation value
     * @param <T>   the type of the success value
     * @param <E>   the error type
     */
    record Success<T, E>(T value) implements Result<T, E> {}

    /**
     * Failure outcome record holding an error of type {@code E}.
     *
     * @param error the failure description or error object
     * @param <T>   the success type
     * @param <E>   the type of the error
     */
    record Failure<T, E>(E error) implements Result<T, E> {}

    /**
     * Creates a successful {@link Result} holding the specified value.
     *
     * @param value the successful result value
     * @param <T>   the success value type
     * @param <E>   the error type
     * @return a {@link Result.Success} instance
     */
    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failure {@link Result} holding the specified error.
     *
     * @param error the error instance
     * @param <T>   the success value type
     * @param <E>   the error type
     * @return a {@link Result.Failure} instance
     */
    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    /**
     * Checks if this result represents a success.
     *
     * @return {@code true} if successful, {@code false} otherwise
     */
    default boolean isSuccess() {
        return this instanceof Success<?, ?>;
    }

    /**
     * Checks if this result represents a failure.
     *
     * @return {@code true} if failure, {@code false} otherwise
     */
    default boolean isFailure() {
        return this instanceof Failure<?, ?>;
    }

    /**
     * Returns an {@link Optional} containing the success value if present, or empty if failure.
     *
     * @return an {@link Optional} of the success value
     */
    default Optional<T> success() {
        if (this instanceof Success<?, ?> success) {
            T value = ((Success<T, E>) success).value();
            return Optional.of(value);
        }
        return Optional.empty();
    }

    /**
     * Returns an {@link Optional} containing the failure error if present, or empty if success.
     *
     * @return an {@link Optional} of the failure error
     */
    default Optional<E> failure() {
        if (this instanceof Failure<?, ?> failure) {
            E error = ((Failure<T, E>) failure).error();
            return Optional.of(error);
        }
        return Optional.empty();
    }

    /**
     * Transforms this result into a value of type {@code R} by applying {@code onSuccess} if
     * successful, or {@code onFailure} if failed.
     *
     * @param onSuccess function to apply to the success value
     * @param onFailure function to apply to the failure error
     * @param <R>       the return type of the folding operation
     * @return the transformed result value
     */
    default <R> R fold(Function<? super T, ? extends R> onSuccess,
                       Function<? super E, ? extends R> onFailure) {
        if (this instanceof Success<?, ?> success) {
            T value = ((Success<T, E>) success).value();
            return onSuccess.apply(value);
        }
        E error = ((Failure<T, E>) this).error();
        return onFailure.apply(error);
    }
}
