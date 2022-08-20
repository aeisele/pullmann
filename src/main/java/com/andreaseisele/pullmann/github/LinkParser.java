package com.andreaseisele.pullmann.github;

import static java.util.Objects.requireNonNull;


import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkParser {

    private static final Logger logger = LoggerFactory.getLogger(LinkParser.class);

    private static final Pattern PATTERN_LINK = Pattern.compile("<?(?<url>[^>]*)>;\\Wrel=\"(?<rel>.*)\"");

    /**
     * Parse Link value and return rel of name 'last'.
     * @param linkHeader a link header value
     * @return the 'last' rel of available
     */
    // Link: <https://api.github.com/repositories/2325298/pulls?page=2>; rel="next", <https://api.github.com/repositories/2325298/pulls?page=11>; rel="last"
    public static Optional<String> getLastRel(String linkHeader) {
        requireNonNull(linkHeader, "link header must not be null");

        final var links = linkHeader.split(",");
        for (String link : links) {
            if (link.contains("last")) {
                final var matcher = PATTERN_LINK.matcher(link.trim());
                if (matcher.matches()) {
                    return Optional.of(matcher.group("url"));
                }
            }
        }

        logger.warn("could find 'last' rel in Link header value: [{}]", linkHeader);
        return Optional.empty();
    }

    private LinkParser(){}

}
