package com.andreaseisele.pullmann.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpResponse;

public class JsonBodyHandler<T> implements HttpResponse.BodyHandler<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> type;

    public static <T> JsonBodyHandler<T> jsonBodyHandler(ObjectMapper objectMapper, Class<T> type) {
        return new JsonBodyHandler<>(objectMapper, type);
    }

    private JsonBodyHandler(ObjectMapper objectMapper, Class<T> type) {
        this.objectMapper = objectMapper;
        this.type = type;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofByteArray(),
            bytes -> {
                try {
                    if (bytes.length > 0) {
                        return objectMapper.readValue(bytes, type);
                    } else {
                        return null;
                    }
                } catch (IOException ioe) {
                    throw new RestClientException("error unmarshalling JSON body", ioe);
                }
            });
    }
}
