package com.andreaseisele.pullmann.github.result;

import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserResultTest {

    @Test
    void parseExpiry_normal() {
        final String expiryValue = "2022-09-15 15:06:14 UTC";
        final LocalDateTime expected = ZonedDateTime.of(2022, 9, 15, 15, 6, 14, 0,
                ZoneId.of("UTC"))
            .toLocalDateTime();
        final LocalDateTime expiry = UserResult.parseExpiry(expiryValue);

        assertThat(expiry).isEqualTo(expected);
    }

    @Test
    void parseExpiry_wrongFormat() {
        final String expiryValue = "2022-09-15 15:06 UTC"; // seconds missing

        final LocalDateTime parsed = UserResult.parseExpiry(expiryValue);

        assertThat(parsed).isNull();
    }

    @Test
    void parseScopeList_normal() {
        final String scopeList = "public_repo, read:user, repo:status, user:email";

        final Set<String> scopes = UserResult.parseScopeList(scopeList);

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
        final Set<String> scopes = UserResult.parseScopeList(scopeList);

        assertThat(scopes).isEmpty();
    }

    @Test
    void parseScopeList_withBlanks() {
        final String scopeList = "public_repo, , read:user, repo:status, , user:email";

        final Set<String> scopes = UserResult.parseScopeList(scopeList);

        assertThat(scopes).containsOnly("public_repo", "read:user", "repo:status", "user:email");
    }

    @Test
    void parseScopeList_null() {
        final Set<String> scopes = UserResult.parseScopeList(null);

        assertThat(scopes)
            .isNotNull()
            .isEmpty();
    }
}