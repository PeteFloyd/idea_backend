package com.learn.demo.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.learn.demo.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {SecurityConfig.class, SecurityConfigTest.TestConfig.class})
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void securityBeansArePresent() {
        assertNotNull(securityFilterChain);
        assertNotNull(passwordEncoder);
    }

    @Configuration
    static class TestConfig {
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(null, null);
        }
    }
}
