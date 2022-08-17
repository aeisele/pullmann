package com.andreaseisele.pullmann.github;

import com.andreaseisele.pullmann.github.dto.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GitHubClient {

    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    private final GitHubProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitHubClient(GitHubProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    public User currentUser() {
        final var uri = UriComponentsBuilder.fromUriString(properties.baseUrl()).path("user").build().toUri();
        final var request = HttpRequest.newBuilder(uri)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .GET()
            .build();
        try {
            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), User.class);
        } catch (IOException ioe) {
            logger.error("error sending 'currentUser' request", ioe);
        } catch (InterruptedException ie) {
            logger.warn("interrupted on sending 'currentUser' request");
            Thread.currentThread().interrupt();
        }

        throw new RuntimeException("todo");
    }

}
