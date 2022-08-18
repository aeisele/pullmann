package com.andreaseisele.pullmann.github;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import okhttp3.logging.HttpLoggingInterceptor;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "pullman.github")
public final class GitHubProperties {

    @URL
    @NotNull
    private String baseUrl = "https://api.github.com";

    @Valid
    @NestedConfigurationProperty
    @NotNull
    private Timeouts timeouts = new Timeouts();

    @NotNull
    private HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.NONE;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
    }

    public HttpLoggingInterceptor.Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(HttpLoggingInterceptor.Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * HTTP Client Timeouts
     * see <a href="https://square.github.io/okhttp/recipes/#timeouts-kt-java">Timeouts</a>
     */
    public static class Timeouts {

        @PositiveOrZero
        private int connectSeconds = 10;

        @PositiveOrZero
        private int writeSeconds = 5;

        @PositiveOrZero
        private int readSeconds = 5;

        @PositiveOrZero
        private int callSeconds = 5;

        public int getConnectSeconds() {
            return connectSeconds;
        }

        public void setConnectSeconds(int connectSeconds) {
            this.connectSeconds = connectSeconds;
        }

        public int getWriteSeconds() {
            return writeSeconds;
        }

        public void setWriteSeconds(int writeSeconds) {
            this.writeSeconds = writeSeconds;
        }

        public int getReadSeconds() {
            return readSeconds;
        }

        public void setReadSeconds(int readSeconds) {
            this.readSeconds = readSeconds;
        }

        public int getCallSeconds() {
            return callSeconds;
        }

        public void setCallSeconds(int callSeconds) {
            this.callSeconds = callSeconds;
        }
    }

}
