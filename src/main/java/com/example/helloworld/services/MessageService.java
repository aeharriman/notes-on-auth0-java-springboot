package com.example.helloworld.services;

import org.springframework.stereotype.Service;

import com.example.helloworld.models.Message;

// A note. Nowhere in this app are admin and protected treated differently, which is confusing given it looks like they were trying to implement Role-based access.
@Service
public record MessageService() {

  public Message getPublicMessage() {
    final var text = "This is a public message.";

    return Message.from(text);
  }

  public Message getProtectedMessage() {
    final var text = "This is a protected message.";

    return Message.from(text);
  }

  public Message getAdminMessage() {
    final var text = "This is an admin message.";

    return Message.from(text);
  }
}
