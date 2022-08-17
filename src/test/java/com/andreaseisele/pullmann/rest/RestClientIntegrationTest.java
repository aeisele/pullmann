package com.andreaseisele.pullmann.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static org.assertj.core.api.Assertions.assertThat;


import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration-test")
@WireMockTest
class RestClientIntegrationTest {

    @Autowired
    private RestClient restClient;

    private String baseUrl;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        this.baseUrl = "http://localhost:" + wmRuntimeInfo.getHttpPort();
    }

    @Test
    void getStatusOk() {
        stubFor(get("/test-path")
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(ok()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{ \"intValue\" : 123, \"strValue\" : \"test\" }")));

        final var response = restClient.get(URI.create(baseUrl + "/test-path"), TestDto.class);
        assertThat(response).isNotNull();

        final var dto = response.body();
        assertThat(dto).isNotNull();
        assertThat(dto.intValue()).isEqualTo(123);
        assertThat(dto.strValue()).isEqualTo("test");
    }

    @Test
    void getOkThroughRedirect() {
        stubFor(get("/test-path")
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(ok()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{ \"intValue\" : 123, \"strValue\" : \"test\" }")));
        stubFor(get("redirect-me")
            .willReturn(temporaryRedirect("/test-path")));

        final var response = restClient.get(URI.create(baseUrl + "/test-path"), TestDto.class);
        assertThat(response).isNotNull();

        final var dto = response.body();
        assertThat(dto).isNotNull();
        assertThat(dto.intValue()).isEqualTo(123);
        assertThat(dto.strValue()).isEqualTo("test");
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403})
    void getStatus4xx(int status) {
        stubFor(get("/test-path")
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(status(status)));

        final var response = restClient.get(URI.create(baseUrl + "/test-path"), TestDto.class);

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(response.body()).isNull();
    }


    @ParameterizedTest
    @ValueSource(ints = {500, 503})
    void getStatus5xx(int status) {
        stubFor(get("/test-path")
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(status(status)));

        final var response = restClient.get(URI.create(baseUrl + "/test-path"), TestDto.class);

        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(response.body()).isNull();
    }

    @Test
    void postStatusOk() {
        stubFor(post("/test-path")
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withRequestBody(equalToJson("{ \"intValue\" : 456, \"strValue\" : \"test-request\" }"))
            .willReturn(ok()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{ \"intValue\" : 123, \"strValue\" : \"test\" }")));
        final var requestDto = new TestDto(456, "test-request");

        final var response = restClient.post(URI.create(baseUrl + "/test-path"), TestDto.class, requestDto);
        assertThat(response).isNotNull();

        final var dto = response.body();
        assertThat(dto).isNotNull();
        assertThat(dto.intValue()).isEqualTo(123);
        assertThat(dto.strValue()).isEqualTo("test");
    }

    record TestDto(int intValue, String strValue){}

}