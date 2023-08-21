# Spring/Java: API Basic Access Control Code Sample

This Java code sample demonstrates **how to implement authorization** in Spring API servers using Auth0. This Spring code sample builds the API server using Spring functional controllers.

This code sample is part of the ["Auth0 Developer Resources"](https://developer.auth0.com/resources), a place where you can explore the authentication and authorization features of the Auth0 Identity Platform.

Visit the ["Spring/Java + Functional Controllers Code Sample: Authorization For Basic APIs"](https://developer.auth0.com/resources/code-samples/api/spring/basic-authorization/java-functional) page for instructions on how to configure and run this code sample and how to integrate it with a Single-Page Application (SPA) of your choice.

## Why Use Auth0?

Auth0 is a flexible drop-in solution to add authentication and authorization services to your applications. Your team and organization can avoid the cost, time, and risk that come with building your own solution to authenticate and authorize users. We offer tons of guidance and SDKs for you to get started and [integrate Auth0 into your stack easily](https://developer.auth0.com/resources/code-samples/full-stack).

### The Auth2.0 process
```mermaid
sequenceDiagram
    participant User
    participant Client
    participant Authorization Server
    participant Resource Server

    User->>Client: Request resource
    Client->>User: Redirect to Authorization Server
    User->>Authorization Server: Authenticate & grant permission
    Authorization Server->>User: Return authorization code
    User->>Client: Provide authorization code
    Client->>Authorization Server: Request access token using authorization code
    Authorization Server->>Client: Provide access token
    Client->>Resource Server: Request resource with access token
    Resource Server->>Client: Provide requested resource
    Client->>User: Display resource
```

### Including the Auth0 process
```mermaid
sequenceDiagram
    participant User
    participant Client
    participant Auth0 as Auth0 (Authorization Server)
    participant IdP as Identity Provider (IdP)
    participant Resource Server

    User->>Client: Request resource
    Client->>User: Redirect to Auth0 Universal Login
    User->>Auth0: Choose Identity Provider (e.g., Google, LinkedIn)
    Auth0->>IdP: Redirect user for authentication
    User->>IdP: Authenticate & grant permission
    IdP->>Auth0: Return authentication result
    Auth0->>User: Redirect with authorization code (and sync user profile data)
    User->>Client: Provide authorization code
    Client->>Auth0: Request access token using authorization code
    Auth0->>Client: Provide access token
    Client->>Resource Server: Request resource with access token
    Resource Server->>Client: Provide requested resource
    Client->>User: Display resource
```


### Updated with transferred info and related protocols
```mermaid
sequenceDiagram
    participant User
    participant Client as Client / Client backend
    participant Auth0 as Auth0 Login page/Auth server
    participant IdP as Identity Provider (IdP)
    participant RS as Resource server

    User->>Client: 1. Request resource
    Client->>Auth0: 2. Redirect to Universal Login (OAuth2.0)
    Auth0->>User: 3. Choose Identity Provider (e.g., Google, LinkedIn)
    User->>Auth0: 4. Authenticate via chosen IdP (OIDC)
    Auth0->>IdP: 5. Redirect user for authentication (OIDC)
    User->>IdP: 6. Authenticate & grant permission (OIDC)
    IdP->>Auth0: 7. Send authentication assertion with user data (JWT) (OIDC)
    Note over Auth0: Auth0 grants Authorization code.<br/>It's used to ensure that the client application<br/>that initiated the authentication is the same one<br/>receiving the tokens.
    Auth0->>User: 8. Redirect user's browser to client redirect uri <br/>authorization code as query parameter (JWT)(OAuth2.0)
    User->>Client: 9. authorization code (OAuth2.0)
    Client->>Auth0: 10. Client's backend hits /tokens<br/> authorization code (OAuth2.0)<br/> Client ID<br/> Client secret
    Auth0->>Client: 11. ID Token based on auth assertion info (JWT) (OIDC)<br/>& Access Token (JWT) (OAuth2.0)
    Client->>RS: 12. Request resource with Access Token (OAuth2.0).<br/>Note: Resource server may or may not be same as client backend depending on structure
    RS->>Client: 13. Provide requested resource (OAuth2.0)
    Client->>User: Display resource
```

Original remote:
https://github.com/auth0-developer-hub/api_spring_java_hello-world_functional.git