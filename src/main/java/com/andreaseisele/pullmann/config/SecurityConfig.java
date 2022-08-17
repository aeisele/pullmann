package com.andreaseisele.pullmann.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain secFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeRequests()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
            .antMatchers("/webfonts/*").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin().loginPage("/login").permitAll()
            .and()
            .logout().permitAll()
            .and().build();
    }

}
