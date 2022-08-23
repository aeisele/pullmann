package com.andreaseisele.pullmann.domain;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RepositoryNameTest {

    static Stream<Arguments> argsForOk() {
        return Stream.of(
            Arguments.of("octocat/Hello-World", "octocat", "Hello-World"),
            Arguments.of("   blank-user/repo", "blank-user", "repo"),
            Arguments.of("user/blank-repo    ", "user", "blank-repo"),
            Arguments.of("funkyUser0815/repo.repo", "funkyUser0815", "repo.repo")
        );
    }

    @ParameterizedTest
    @MethodSource("argsForOk")
    void parseOk(String fullName, String expectedOwner, String expectedRepo) {
        final var parsed = RepositoryName.parse(fullName);

        assertThat(parsed).hasValueSatisfying(name -> {
            assertThat(name.owner()).isEqualTo(expectedOwner);
            assertThat(name.repository()).isEqualTo(expectedRepo);
        });
    }

    @Test
    void parseFail() {
        final var parsed = RepositoryName.parse("not a repo full name");

        assertThat(parsed).isEmpty();
    }

}