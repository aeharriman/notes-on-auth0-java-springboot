package com.example.helloworld.config;

import static org.springframework.web.servlet.function.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import com.example.helloworld.handlers.MessageHandler;

@Configuration // spring scans this class for beans
public class Router {

  // Spring MVC: Controller-Service-Repo
  // Spring WebFlux: Router-Handler-Repo

  // This bean routes incoming HTTP requests to appropriate handler methods. It's kind of like a controller
  @Bean
  // Spring WebFlux version of RequestMapping
  public RouterFunction<ServerResponse> apiRouter(
    final MessageHandler messageHandler,
    final GlobalErrorHandler globalErrorHandler
  ) {
    final var api = Paths.apiPath();
    final var messages = api.messagesPath();

    return route()
            // base path /api
      .path(api.segment(), () ->
        route()
                // /api/messages
          .path(messages.segment(), () ->
            route()
              .GET(messages.publicPath().segment(), messageHandler::getPublic)
              .GET(messages.protectedPath().segment(), messageHandler::getProtected)
              .GET(messages.adminPath().segment(), messageHandler::getAdmin)
              .build()
          )
          .build()
      )
      .onError(Throwable.class, globalErrorHandler::handleInternalError)
      .build();
  }
}
