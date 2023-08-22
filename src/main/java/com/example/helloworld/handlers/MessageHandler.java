package com.example.helloworld.handlers;

import static org.springframework.web.servlet.function.ServerResponse.ok;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import com.example.helloworld.services.MessageService;

import lombok.RequiredArgsConstructor;

// This is a handler class that contains methods to process the requests. These methods get called by the router
// It's almost like the 2nd half of the controller, where the service methods get called.
// Except services are optional, and you can put your business logic here if you want. But this app does have a service.

@Component
@RequiredArgsConstructor
public class MessageHandler {

  private final MessageService messageService;

  @Cacheable
  public ServerResponse getPublic(final ServerRequest request) {
    final var message = messageService.getPublicMessage();

    return ok().body(message);
  }

  @Cacheable
  public ServerResponse getProtected(final ServerRequest request) {
    final var message = messageService.getProtectedMessage();

    return ok().body(message);
  }

  @Cacheable
  public ServerResponse getAdmin(final ServerRequest request) {
    final var message = messageService.getAdminMessage();

    return ok().body(message);
  }
}
