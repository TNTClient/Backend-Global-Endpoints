package com.jeka8833.tntclientwebendpoints.web.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final List<String> corsSites;

    SecurityConfig(@Value("${spring.security.cors.sites}") List<String> corsSites) {
        this.corsSites = corsSites;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsSites);
        configuration.addAllowedHeader("*");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/api/v1/player/profile/cape").hasAuthority("CAPE")
                        .requestMatchers("api/v1/player/profile/tab").hasAuthority("HEART")
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true))
                .sessionManagement(session -> session
                        .maximumSessions(3)
                        .sessionRegistry(sessionRegistry()))
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeParameter("remember")
                        .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(7)))
                .build();
    }
}
