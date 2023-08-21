package com.example.helloworld.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/** This class serves as a type-safe way to access the properties defined in application.yml.
 * Instead of manually fetching and parsing values from the configuration file,
 * Spring Boot provides a mechanism to automatically bind these properties to Java objects.
 * This makes accessing and using these properties in the code much cleaner and safer.
 * It is similar to using the @Value annotation, but at a higher level. */
@ConstructorBinding
@ConfigurationProperties(prefix = "application")
public record ApplicationProperties(String audience, String clientOriginUrl) {

}
