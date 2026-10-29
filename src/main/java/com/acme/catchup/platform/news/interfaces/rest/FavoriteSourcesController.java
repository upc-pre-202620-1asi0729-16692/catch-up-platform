package com.acme.catchup.platform.news.interfaces.rest;

import com.acme.catchup.platform.news.application.commandservices.FavoriteSourceCommandService;
import com.acme.catchup.platform.news.application.queryservices.FavoriteSourceQueryService;
import com.acme.catchup.platform.news.domain.model.aggregates.FavoriteSource;
import com.acme.catchup.platform.news.domain.model.queries.GetAllFavoriteSourcesByNewsApiKeyQuery;
import com.acme.catchup.platform.news.domain.model.queries.GetFavoriteSourceByIdQuery;
import com.acme.catchup.platform.news.domain.model.queries.GetFavoriteSourceByNewsApiKeyAndSourceIdQuery;
import com.acme.catchup.platform.news.interfaces.rest.resources.CreateFavoriteSourceResource;
import com.acme.catchup.platform.news.interfaces.rest.resources.FavoriteSourceResource;
import com.acme.catchup.platform.news.interfaces.rest.transform.CreateFavoriteSourceCommandFromResourceAssembler;
import com.acme.catchup.platform.news.interfaces.rest.transform.NewsValueObjectFromStringAssembler;
import com.acme.catchup.platform.news.interfaces.rest.transform.ResponseEntityFromFavoriteSourceCommandResultAssembler;
import com.acme.catchup.platform.news.interfaces.rest.transform.ResponseEntityFromFavoriteSourceQueryResultAssembler;
import com.acme.catchup.platform.shared.application.patterns.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Inbound service in the interface layer for the favorite sources bounded context.
 * Orchestrates command and query operations through the application layer:
 * POST operations delegate to {@link FavoriteSourceCommandService}, which returns
 * a {@link Result} describing success or duplicate conflict; GET operations delegate
 * to {@link FavoriteSourceQueryService}. Translates between HTTP Resources and domain
 * models using assemblers that cross the interface/domain boundary.
 *
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/favorite-sources", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Favorite Sources", description = "Endpoints for favorite sources")
public class FavoriteSourcesController {
    private static final int MAX_QUERY_PARAM_LENGTH = 256;
    private static final Pattern QUERY_PARAM_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]+$");

    private final FavoriteSourceCommandService favoriteSourceCommandService;
    private final FavoriteSourceQueryService favoriteSourceQueryService;
    private final MessageSource messageSource;

    /**
     * Constructor for FavoriteSourcesController.
     *
     * @param favoriteSourceCommandService Favorite source command service
     * @param favoriteSourceQueryService   Favorite source query service
     * @param messageSource                source for localized response messages
     * @see FavoriteSourceCommandService
     * @see FavoriteSourceQueryService
     * @since 1.0
     */
    public FavoriteSourcesController(FavoriteSourceCommandService favoriteSourceCommandService, FavoriteSourceQueryService favoriteSourceQueryService, MessageSource messageSource) {
        this.favoriteSourceCommandService = favoriteSourceCommandService;
        this.favoriteSourceQueryService = favoriteSourceQueryService;
        this.messageSource = messageSource;
    }

    /**
     * Creates a new favorite source.
     *
     * @param resource the request payload containing the news API key and source ID
     * @return the created favorite source resource, or conflict when the pair already exists
     * @see CreateFavoriteSourceResource
     * @see FavoriteSourceResource
     * @since 1.0
     */
    @Operation(
            summary = "Create a favorite source",
            description = "Creates a favorite source with the provided news API key and source ID",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Favorite source creation request",
                    content = @Content(schema = @Schema(implementation = CreateFavoriteSourceResource.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Favorite source created",
                    content = @Content(schema = @Schema(implementation = FavoriteSourceResource.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - favorite source already exists for the given newsApiKey and sourceId",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<?> createFavoriteSource(@Valid @RequestBody CreateFavoriteSourceResource resource) {
        log.debug("POST /api/v1/favorite-sources – newsApiKey={}, sourceId={}",
                NewsValueObjectFromStringAssembler.toNewsApiKeyFromString(resource.newsApiKey()).maskedForLogging(),
                resource.sourceId());
        var favoriteSource = favoriteSourceCommandService
                .handle(CreateFavoriteSourceCommandFromResourceAssembler.toCommandFromResource(resource));
        var response = ResponseEntityFromFavoriteSourceCommandResultAssembler.toResponseEntityFromResult(favoriteSource, messageSource);
        log.debug("POST /api/v1/favorite-sources – response status={}", response.getStatusCode());
        return response;
    }

    /**
     * Gets a favorite source by ID.
     *
     * @param id Favorite source ID
     * @return ResponseEntity with the favorite source resource if found or not found otherwise
     * @see FavoriteSourceResource
     * @since 1.0
     */
    @Operation(
            summary = "Get a favorite source by ID",
            description = "Gets a favorite source by the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite source found",
                    content = @Content(schema = @Schema(implementation = FavoriteSourceResource.class))),
            @ApiResponse(responseCode = "404", description = "Favorite source not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("{id}")
    public ResponseEntity<?> getFavoriteSourceById(@PathVariable Long id) {
        log.debug("GET /api/v1/favorite-sources/{}", id);
        Optional<FavoriteSource> favoriteSource = favoriteSourceQueryService.handle(new GetFavoriteSourceByIdQuery(id));
        if (favoriteSource.isEmpty()) {
            log.debug("Favorite source not found for id={}", id);
            return ResponseEntityFromFavoriteSourceQueryResultAssembler.notFound(
                    messageSource,
                    "favorite.source.error.notFoundById",
                    id);
        }
        return ResponseEntityFromFavoriteSourceQueryResultAssembler.toResponseEntityFromFavoriteSource(favoriteSource.orElseThrow());
    }

    /**
     * Gets all favorite sources by news API key.
     *
     * @param newsApiKey News API key
     * @return ResponseEntity with the list of favorite source resources if found or not found otherwise
     * @see FavoriteSourceResource
     * @since 1.0
     */
    private ResponseEntity<?> getAllFavoriteSourcesByNewsApiKey(String newsApiKey) {
        var getAllFavoriteSourcesByNewsApiKeyQuery = new GetAllFavoriteSourcesByNewsApiKeyQuery(
                NewsValueObjectFromStringAssembler.toNewsApiKeyFromString(newsApiKey));
        var favoriteSources = favoriteSourceQueryService.handle(getAllFavoriteSourcesByNewsApiKeyQuery);
        return ResponseEntityFromFavoriteSourceQueryResultAssembler.toResponseEntityFromList(favoriteSources);
    }

    /**
     * Gets a favorite source by news API key and source ID.
     *
     * @param newsApiKey News API key
     * @param sourceId   Source ID
     * @return ResponseEntity with the favorite source resource if found or not found otherwise
     * @see FavoriteSourceResource
     * @since 1.0
     */
    private ResponseEntity<?> getFavoriteSourceByNewsApiKeyAndSourceId(String newsApiKey, String sourceId) {
        var getFavoriteSourceByNewsApiKeyAndSourceIdQuery = new GetFavoriteSourceByNewsApiKeyAndSourceIdQuery(
                NewsValueObjectFromStringAssembler.toNewsApiKeyFromString(newsApiKey),
                NewsValueObjectFromStringAssembler.toSourceIdFromString(sourceId));
        var favoriteSource = favoriteSourceQueryService.handle(getFavoriteSourceByNewsApiKeyAndSourceIdQuery);
        if (favoriteSource.isEmpty()) {
            log.debug("Favorite source not found for newsApiKey={}, sourceId={}",
                    NewsValueObjectFromStringAssembler.toNewsApiKeyFromString(newsApiKey).maskedForLogging(),
                    sourceId);
            return ResponseEntityFromFavoriteSourceQueryResultAssembler.notFound(
                    messageSource,
                    "favorite.source.error.notFoundByNewsApiKeyAndSourceId");
        }
        return ResponseEntityFromFavoriteSourceQueryResultAssembler.toResponseEntityFromFavoriteSource(favoriteSource.orElseThrow());
    }

    /**
     * Retrieves favorite source data using query parameters.
     * When both {@code newsApiKey} and {@code sourceId} are provided, a single
     * favorite source is searched. When only {@code newsApiKey} is provided, all
     * matching favorite sources are returned.
     * @param newsApiKey required news API key query parameter
     * @param sourceId optional source identifier query parameter
     * @return a single favorite source, a list of favorite sources, or a bad request problem detail
     * @see FavoriteSourceResource
     * @since 1.0
     */
    @Operation(
            summary = "Get favorite sources with parameters (News API key and optionally Source ID)",
            description = "Gets favorite sources based on the provided parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite source(s) found",
                    content = @Content(schema = @Schema(oneOf = {FavoriteSourceResource.class, FavoriteSourceResource[].class}))),
            @ApiResponse(responseCode = "404", description = "Favorite source(s) not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<?> getFavoriteSourcesWithParameters(
            @Parameter(name = "newsApiKey", description = "News API key", required = true,
                    schema = @Schema(maxLength = 256, pattern = "^[A-Za-z0-9._:-]+$"))
            @RequestParam(name = "newsApiKey", required = false) String newsApiKey,
            @Parameter(name = "sourceId", description = "Source ID",
                    schema = @Schema(maxLength = 256, pattern = "^[A-Za-z0-9._:-]+$"))
            @RequestParam(name = "sourceId", required = false) String sourceId) {
        if (newsApiKey != null && isInvalidQueryParam(newsApiKey)) {
            log.warn("Rejected invalid query param newsApiKey (value omitted for security)");
            return ResponseEntityFromFavoriteSourceQueryResultAssembler.badRequest(
                    messageSource,
                    "favorite.source.error.newsApiKey.invalid");
        }
        if (sourceId != null && isInvalidQueryParam(sourceId)) {
            log.warn("Rejected invalid query param sourceId (value omitted for security)");
            return ResponseEntityFromFavoriteSourceQueryResultAssembler.badRequest(
                    messageSource,
                    "favorite.source.error.sourceId.invalid");
        }
        if (newsApiKey != null && !newsApiKey.isBlank() && sourceId != null && !sourceId.isBlank()) {
            log.debug("GET /api/v1/favorite-sources?newsApiKey={}&sourceId={}",
                    NewsValueObjectFromStringAssembler.toNewsApiKeyFromString(newsApiKey).maskedForLogging(),
                    sourceId);
            return getFavoriteSourceByNewsApiKeyAndSourceId(newsApiKey, sourceId);
        } else if (newsApiKey != null && !newsApiKey.isBlank()) {
            log.debug("GET /api/v1/favorite-sources?newsApiKey={}",
                    NewsValueObjectFromStringAssembler.toNewsApiKeyFromString(newsApiKey).maskedForLogging());
            return getAllFavoriteSourcesByNewsApiKey(newsApiKey);
        } else {
            log.warn("GET /api/v1/favorite-sources – missing or blank required parameter: newsApiKey");
            return ResponseEntityFromFavoriteSourceQueryResultAssembler.badRequest(
                    messageSource,
                    "favorite.source.error.newsApiKey.missing");
        }
    }

    /**
     * Validates a query parameter value against blank, size, and allowed-character constraints.
     *
     * @param value query parameter value to validate
     * @return {@code true} when the value is invalid; otherwise {@code false}
     */
    private boolean isInvalidQueryParam(String value) {
        return value.isBlank() || value.length() > MAX_QUERY_PARAM_LENGTH || !QUERY_PARAM_PATTERN.matcher(value).matches();
    }

}
