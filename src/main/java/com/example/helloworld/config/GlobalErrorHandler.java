package com.example.helloworld.config;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import com.example.helloworld.models.ErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

// @RestControllerAdvice is an annotation used to define @ExceptionHandler, @InitBinder, and @ModelAttribute methods
// that apply to all @RequestMapping methods. It's a way to apply exception handling globally across multiple controllers.
@RestControllerAdvice
public record GlobalErrorHandler(ObjectMapper mapper) {

  // This method handles the NoHandlerFoundException, which is thrown when a request is made to an endpoint
  // that doesn't exist in the application. It returns a 404 with a body of a custom error message with a "Not Found" message.
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoHandlerFoundException.class)
  public ErrorMessage handleNotFound(final HttpServletRequest request, final Exception error) {
    return ErrorMessage.from("Not Found");
  }

  // @Cacheable is an annotation that indicates that the result of invoking a method (in this case, handleInternalError)
  // can be cached. The next time the method is called with the same arguments, the cached result is returned
  // instead of invoking the method again. This can improve performance by avoiding unnecessary computations.
  @Cacheable
  public ServerResponse handleInternalError(final Throwable error, final ServerRequest request) {
    // This method handles internal server errors and returns a custom error message with the error's message.
    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ErrorMessage.from(error.getMessage()));
  }

  @Cacheable
  public void handleAuthenticationError(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final AuthenticationException error
  ) throws IOException {
    // This method handles authentication errors. When a user tries to access a protected resource without
    // proper authentication, this method sends a custom error message with a "Requires authentication" message.
    final var errorMessage = ErrorMessage.from("Requires authentication");
    final var json = mapper.writeValueAsString(errorMessage);

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(json);
    response.flushBuffer();
  }
}
