package com.andreaseisele.pullmann.github;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.Test;

class LinkParserTest {

    @Test
    void getLastRel_github() {
        final var linkValue = "<https://api.github.com/repositories/2325298/pulls?page=2>; rel=\"next\", <https://api.github.com/repositories/2325298/pulls?page=11>; rel=\"last\"";

        final var lastRel = LinkParser.getLastRel(linkValue);

        assertThat(lastRel).contains("https://api.github.com/repositories/2325298/pulls?page=11");
    }

    @Test
    void getLastRel_missing() {
        final var linkValue = "<https://api.github.com/repositories/2325298/pulls?page=2>; rel=\"next\", <https://api.github.com/repositories/2325298/pulls?page=11>; rel=\"something_else\"";

        final var lastRel = LinkParser.getLastRel(linkValue);

        assertThat(lastRel).isEmpty();
    }

    @Test
    void getLastRel_invalid() {
        final var linkValue = "this is not a link value obviously";

        final var lastRel = LinkParser.getLastRel(linkValue);

        assertThat(lastRel).isEmpty();
    }

}