package com.example.helloworld.config;

import java.time.Duration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

  private final ApplicationProperties applicationProps;

  @Bean
  // This method configures Cross-Origin Resource Sharing (CORS) for the application.
  // returns an instance of a type that implements the CorsConfigurationSource interface,
  // which has a method that
  // returns a CorsConfiguration based on the incoming request.
  public CorsConfigurationSource corsConfigurationSource() {

    final var source = new UrlBasedCorsConfigurationSource();
    final var config = new CorsConfiguration();
    // Define allowed origins from application properties.
    final var origins = List.of(applicationProps.clientOriginUrl());
    // Defining allowed headers
    final var headers = List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE);
    // Define allowed HTTP methods, in this case, only GET.
    final var methods = List.of(HttpMethod.GET.name());
    // Set the maximum age for the CORS preflight request to be cached.
    // A CORS preflight request is made by browsers to see if the request it is about to send is going to be valid for your CORS configuration.
    // browser uses the OPTIONS HTTP method
    final var maxAge = Duration.ofSeconds(86400);

    // Set to configuration
    config.setAllowedOrigins(origins);
    config.setAllowedHeaders(headers);
    config.setAllowedMethods(methods);
    config.setMaxAge(maxAge);

    // register for all paths
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  // This method creates a CORS filter using the previously defined CORS configuration source.
  // This filter will be applied to incoming requests to handle CORS preflight requests and headers.
  // if the cors check passes, the request enters the SecurityFilterChain from SecurityConfig
  public CorsFilter corsFilter() {
    final var configSource = this.corsConfigurationSource();

    return new CorsFilter(configSource);
  }
}
