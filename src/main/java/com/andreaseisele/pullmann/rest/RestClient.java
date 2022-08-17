package com.andreaseisele.pullmann.rest;

import static com.andreaseisele.pullmann.rest.JsonBodyHandler.jsonBodyHandler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class RestClient {

    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

    private static final Map<String, String> DEFAULT_HEADERS = Map.of(
        HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public <T> HttpResponse<T> get(URI uri, Class<T> responseType) {
        final var requestBuilder = HttpRequest.newBuilder()
            .uri(uri)
            .GET();
        addDefaultHeaders(requestBuilder);

        return send(requestBuilder.build(), jsonBodyHandler(objectMapper, responseType));
    }

    public <T, U> HttpResponse<T> post(URI uri, Class<T> responseType, U requestBody) {
        Objects.requireNonNull(requestBody, "request body must not be null");

        final var bodyJson = marshall(requestBody);
        final var bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyJson);
        final var requestBuilder = HttpRequest.newBuilder()
            .uri(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .POST(bodyPublisher);
        addDefaultHeaders(requestBuilder);

        return send(requestBuilder.build(), jsonBodyHandler(objectMapper, responseType));
    }

    private <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        try {
            return httpClient.send(request, bodyHandler);
        } catch (IOException e) {
            throw new RestClientException("error sending request", e);
        } catch (InterruptedException e) {
            logger.warn("interrupted while sending request");
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private <U> String marshall(U value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RestClientException(String.format("error marshalling [%s] to JSON", value), e);
        }
    }

    private static void addDefaultHeaders(HttpRequest.Builder builder) {
        for (final var entry : DEFAULT_HEADERS.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }
    }

}
