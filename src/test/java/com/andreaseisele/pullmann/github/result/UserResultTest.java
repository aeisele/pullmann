package com.andreaseisele.pullmann.github.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserResultTest {

    @Test
    void parseExpiry_normal() {
        final var expiryValue = "2022-09-15 15:06:14 UTC";
        final var expected = ZonedDateTime.of(2022, 9, 15, 15, 6, 14, 0,
                ZoneId.of("UTC"))
            .toLocalDateTime();
        final var expiry = UserResult.parseExpiry(expiryValue);

        assertThat(expiry).isEqualTo(expected);
    }

    @Test
    void parseExpiry_wrongFormat() {
        final var expiryValue = "2022-09-15 15:06 UTC"; // seconds missing

        assertThatThrownBy(() -> UserResult.parseExpiry(expiryValue))
            .isInstanceOf(DateTimeParseException.class)
            .hasMessageContaining("could not be parsed");
    }

    @Test
    void parseScopeList_normal() {
        final var scopeList = "public_repo, read:user, repo:status, user:email";

        final var scopes = UserResult.parseScopeList(scopeList);

        assertThat(scopes).containsOnly("public_repo", "read:user", "repo:status", "user:email");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "   ",
        "\t ",
        " , "
    })
    void parseScopeList_empty(String scopeList) {
        final var scopes = UserResult.parseScopeList(scopeList);

        assertThat(scopes).isEmpty();
    }

    @Test
    void parseScopeList_withBlanks() {
        final var scopeList = "public_repo, , read:user, repo:status, , user:email";

        final var scopes = UserResult.parseScopeList(scopeList);

        assertThat(scopes).containsOnly("public_repo", "read:user", "repo:status", "user:email");
    }

    @Test
    void parseScopeList_null() {
        final var scopes = UserResult.parseScopeList(null);

        assertThat(scopes)
            .isNotNull()
            .isEmpty();
    }
}