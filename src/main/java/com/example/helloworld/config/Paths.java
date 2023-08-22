package com.example.helloworld.config;

import java.util.Objects;

import org.springframework.lang.Nullable;
import org.springframework.web.util.DefaultUriBuilderFactory;

// sealed means only internal classes/interfaces can implement this
public sealed interface Paths {

  String segment();

  @Nullable Paths root();

  // constructs full URI path by combining the current segment with the root
  default String build() {
    final var factory = new DefaultUriBuilderFactory();
    final var rootPath = root();
    final var prevPath = Objects.nonNull(rootPath)
      ? rootPath.build()
      : "";
    final var uriBuilder = factory.uriString(prevPath);

    return uriBuilder.path(segment())
      .build()
      .getPath();
  }

  // for each segment, makes a new record for the sub-segments. easier to understand when read backwards
  static ApiPaths apiPath() {
    return new ApiPaths("/api", null);
  }

  record ApiPaths(String segment, Paths root) implements Paths {

    // api/messages
    public MessagesPaths messagesPath() {
      return new MessagesPaths("/messages", Paths.apiPath());
    }

    public record MessagesPaths(String segment, Paths root) implements Paths {

      // api/messages/public
      public MessagesEndpoint publicPath() {
        return new MessagesEndpoint("/public");
      }

      // api/messages/protected
      public MessagesEndpoint protectedPath() {
        return new MessagesEndpoint("/protected");
      }

      // api/messages/admin
      public MessagesEndpoint adminPath() {
        return new MessagesEndpoint("/admin");
      }

      public record MessagesEndpoint(String segment, Paths root) implements Paths {

        public MessagesEndpoint(final String segment) {
          this(segment, Paths.apiPath().messagesPath());
        }
      }
    }
  }
}
