package com.example.helloworld.models;

// This is a way of typing an object that contains a message string.
// I don't know why it is necessary. The Api Paths had something like this too.
// I suppose so that you aren't passing a raw string around without knowing the context that it is a message
public record Message(String text) {

  // This is a factory method to produce a Message object without using the new keyword
  public static Message from(final String text) {
    return new Message(text);
  }
}
