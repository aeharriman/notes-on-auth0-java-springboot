package com.example.helloworld.config.security;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.helloworld.config.ApplicationProperties;
import com.example.helloworld.config.GlobalErrorHandler;
import com.example.helloworld.config.Paths;

import lombok.RequiredArgsConstructor;

// This class is configuring the application to act as a Resource Server.
// The resource server has the endpoints with protected information.

@Configuration // Tells spring to scan this class for bean definitions
@RequiredArgsConstructor // Generates constructor requiring all final fields and @NonNull fields.
// This approach is equivalent to marking each field with @Autowired but is cleaner and follows the recommended practice of using constructor injection
public class SecurityConfig {

  // From spring security, represents properties and configurations related to
  // Oauth2.0 Resource server. Spring automatically binds properties from application.yml to this object
  // through @ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver")
  // In this case,
  // security:
  //    oauth2:
  //      resourceserver:
  //        jwt:
  //          issuer-uri: https://${env.AUTH0_DOMAIN}/
  // and in .env AUTH0_DOMAIN=dev-qoohzu1f67jlfdom.us.auth0.com
  // This issuer-uri is to configure our app to receive data from this *Authorization server* that Auth0 has
  // generated for us that talks to both the frontend and backend..
  private final OAuth2ResourceServerProperties resourceServerProps;

  private final ApplicationProperties applicationProps;

  private final GlobalErrorHandler errorHandler;

  // Tells Spring security to ignore all requests to paths except the ones defined
  @Bean
  public WebSecurityCustomizer webSecurity() {
    final var exclusionRegex = "^(?!%s|%s).*$".formatted(
      "/api/messages/protected",
      "/api/messages/admin"
    );

    return web ->
      web.ignoring()
        .regexMatchers(exclusionRegex);
  }


  /**
   * Configures the security filter chain for the application.
   *
   * <ul>
   *   <li>Specifies that requests to /api/messages/protected and /api/messages/admin require authentication.</li>
   *   <li>All other requests are permitted without authentication.</li>
   *   <li>Sets up CORS (Cross-Origin Resource Sharing) with default settings.</li>
   *   <li>Configures the OAuth 2.0 resource server to use JWTs for authentication.</li>
   *   <li>Specifies a custom error handler for authentication errors.</li>
   *   <li>Sets up a custom JWT decoder to validate the tokens.</li>
   * </ul>
   *
   * @param http The HttpSecurity object to configure.
   * @return The configured SecurityFilterChain.
   * @throws Exception If there's an error during configuration.
   */

  @Bean // http here gets injected at runtime because this is a bean
  public SecurityFilterChain httpSecurity(final HttpSecurity http) throws Exception {
    final var messages = Paths.apiPath().messagesPath();

    // requests to protected and admin paths need to be authenticated
    return http.authorizeRequests(authorizeRequests ->
      authorizeRequests
        .antMatchers(messages.protectedPath().build(), messages.adminPath().build())
          .authenticated()
              // All other requests are allowed without authentication
        .anyRequest()
          .permitAll()
    )
            // Sets up cors with default settings.
            // The default CORS configuration will look for a bean of type CorsConfigurationSource.
    .cors(Customizer.withDefaults())
            //configures the application as an OAuth 2.0 resource server.
            // This means the application expects incoming requests to have JWTs (JSON Web Tokens) for authentication.
    .oauth2ResourceServer(oauth2ResourceServer ->
      oauth2ResourceServer
              // Sets up custom error handler
        .authenticationEntryPoint(errorHandler::handleAuthenticationError)
              // And JWT decoder
        .jwt(jwt -> jwt.decoder(makeJwtDecoder()))
    )
    .build();
  }

  private JwtDecoder makeJwtDecoder() {
    // URI of JWT issuer (entity that creates and signs JWT) (in this case the Auth0 Authorization Server)
    final var issuer = resourceServerProps.getJwt().getIssuerUri();
    // Make a decoder based on issuer's location (Type NimbusJwtDecoder)
    final var decoder = JwtDecoders.<NimbusJwtDecoder>fromIssuerLocation(issuer);
    // Make a JWT validator that checks if JWT's issuer matches provided issuer
    final var withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
    // make a delegating token validator (Just a composite of multiple validators).
    // The way this behaves is that it runs the JWT through both and both return a OAuth2TokenValidatorResult
    final var tokenValidator = new DelegatingOAuth2TokenValidator<>(withIssuer, this::withAudience);

    // Decoder uses this validator to ensure incoming JWTs are valid
    decoder.setJwtValidator(tokenValidator);
    return decoder;
  }

  // Custom Validator Used in the tokenValidator above. Checks that the JWT's audience matches the expected.
  // In this case, the audience for this
  private OAuth2TokenValidatorResult withAudience(final Jwt token) {

    // Validators need custom errors to give. Would later be thrown in SecurityFilterChain
    final var audienceError = new OAuth2Error(
      OAuth2ErrorCodes.INVALID_TOKEN,
      "The token was not issued for the given audience",
      "https://datatracker.ietf.org/doc/html/rfc6750#section-3.1"
    );

    // audience from application props got injected from .env to application.yml and injected into ApplicationProperties by spring
    return token.getAudience().contains(applicationProps.audience())
      ? OAuth2TokenValidatorResult.success()
      : OAuth2TokenValidatorResult.failure(audienceError);
  }
}
