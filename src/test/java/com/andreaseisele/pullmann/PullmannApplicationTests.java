package com.andreaseisele.pullmann;

import static org.assertj.core.api.Assertions.assertThat;


import com.andreaseisele.pullmann.github.GitHubProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("integration-test")
class PullmannApplicationTests {

    @Test
    void contextLoads(ApplicationContext applicationContext) {
        final var properties = applicationContext.getBean(GitHubProperties.class);
        assertThat(properties).as("configuration properties bean").isNotNull();
    }

}
