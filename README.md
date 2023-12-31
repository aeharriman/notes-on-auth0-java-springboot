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
    participant User as User/Browser
    participant Client as React Frontend
    participant Auth0 as Auth0 Login page/Auth server
    participant IdP as Identity Provider (IdP)
    participant RS as Resource server

    User->>Client: 1. Request resource
    Client->>Auth0: 2. Redirect to Universal Login (OAuth2.0)
    Auth0->>User: 3. Login page displays in browser
    User->>Auth0: 4. Choose Identity Provider (e.g., Google, LinkedIn)
    Auth0->>IdP: 5. Redirect user for authentication (OIDC)
    User->>IdP: 6. Authenticate & grant permission (OIDC)
    IdP->>Auth0: 7. Send authentication assertion with user data (JWT) (OIDC)
    Note over Auth0: 8. Auth0 grants Authorization code.<br/>It's used to ensure that the client application<br/>that initiated the authentication is the same one<br/>receiving the tokens.
    Auth0->>User: 9. Redirect user's browser to client redirect uri <br/>authorization code as query parameter (JWT)(OAuth2.0)
    User->>Client: 10. authorization code (OAuth2.0)
    Client->>Auth0: 11. auth0-react library hits /tokens<br/> authorization code (OAuth2.0)<br/> Client ID<br/> Client secret
    Auth0->>Client: 12. ID Token based on auth assertion info (JWT) (OIDC)<br/>& Access Token (JWT) (OAuth2.0)
    Client->>RS: 13. Request resource with Access Token (OAuth2.0)
    RS->>Client: 14. Provide requested resource (OAuth2.0)
    Client->>User: 15. Display resource
```

Original remote:
https://github.com/auth0-developer-hub/api_spring_java_hello-world_functional.git

# Walkthrough of an HTTP request in the application:

## 1. CORS Preflight Request (if needed):
- **Description**:
    - Before the actual request, the browser might send a CORS preflight request (using the `OPTIONS` HTTP method) to check if it's safe to send the actual request.
- **Action**:
    - The `CorsFilter` in `ApplicationConfig` checks this preflight request against the CORS configuration you've set up. If the request's origin, headers, methods, etc., are not allowed by your CORS configuration, the browser will not send the actual request.

## 2. Security Filter Chain:
- **Description**:
    - If the CORS check passes, the browser makes an actual request, which enters the `SecurityFilterChain` in the SecurityConfig.
- **Action**:
    - The `HttpSecurity` configuration in `SecurityConfig` is applied. This configuration dictates how different types of requests should be handled.

### 2.a. Endpoint Authentication:
- **Description**:
    - For routes like `/api/messages/protected` and `/api/messages/admin`, authentication is required.
- **Action**:
    - If the request doesn't have a valid JWT, it will be denied. The JWT is decoded using the `JwtDecoder`. This is where the JWT's signature is verified against the public key of the issuer (Auth0 in this case). If the signature doesn't match, the token is considered tampered and is rejected.

### 2.b. JWT Validation:
- **Description**:
    - After decoding, the JWT's claims (like issuer and audience) are validated.
- **Action**:
- Note. JWTS are Base64Url encoded not for encryption, but to be safe from special character issues with urls
    - The issuer is checked to ensure it matches the expected issuer (from Auth0). The audience is checked to ensure the token is intended for this application. If any of these checks fail, the request is denied.

## 3. Processing the Request:
- **Description**:
    - If all checks pass, the request is forwarded to the appropriate controller or handler method to be processed.
- **Action**:
    - The response is then sent back through the `SecurityFilterChain`, (NOT sure on this yet) which might apply additional processing or headers to the response.

## 4. Response:
- **Description**:
    - The final step where the response is crafted and sent back to the client. More detail TBD

---

# Regarding the private/public key:

- **Description**:
    - When using JWTs for authentication, the token is signed by the issuer using a private key.
- **Action**:
    - The recipient (your application) then verifies the JWT's signature using the issuer's public key. This ensures that the JWT was indeed issued by the expected issuer and wasn't tampered with. In the case of Auth0, the public key is often fetched from a well-known endpoint provided by Auth0. The application doesn't need to know the private key; it only needs the public key to verify the JWT's signature.


## Visualization:

```mermaid
graph TD
    A[HTTP Request] --> B[CORS Check]
    B --> |Pass| C[Security Filter Chain]
    B --> |Fail| Z[Denied due to CORS]
    C --> D[Path Exclusion Check]
    D --> |Excluded Path| E[Allow without further checks]
    D --> |Protected/Admin Path| F[Authentication Check]
    F --> |Has JWT| G[JWT Decoding & Validation]
    G --> |Valid JWT| H[Access to Resource]
    G --> |Invalid JWT| I[Error Handling]
    F --> |No JWT| I
    I --> J[Denied due to Authentication Error]
```
### Composite sequence diagram
```mermaid
sequenceDiagram
  participant Http as HTTP/Browser
  participant CorsF as CORS Filter (ApplicationConfig)
  participant SecFChain as Security Filter Chain (SecurityConfig)
  participant Router as Router
  participant RespHeaders as Response Headers Filter
  participant Handler as Handler
  participant Service as Service

  Http->>CorsF: Preflight Request<br/> Could be cached or unnecessary
  Note over Http,CorsF: OPTIONS /api/messages/protected<br/>Origin: http://localhost:4040<br/>Access-Control-Request-Method: GET<br/>Access-Control-Request-Headers: Authorization, Content-Type
  CorsF->>Http: Preflight Response
  Note over CorsF,Http: Status: 200 (OK)<br/>Access-Control-Allow-Origin: http://localhost:4040<br/>Access-Control-Allow-Methods: GET<br/>Access-Control-Allow-Headers: Authorization, Content-Type<br/>Access-Control-Max-Age: 86400<br/>OR <br/>403 Forbidden (Browser doesn't make actual request)
  Http->>SecFChain: Send Actual Request
  Note over Http,SecFChain: GET /api/messages/protected<br/>Authorization: Bearer (JWT access token)
  SecFChain->>SecFChain: Path Exclusion Check for /protected and /admin
  Note over SecFChain: Authentication Check (JWT)<br/>NimbusJwtDecoder automatically:<br/>Fetches public key from JWKS endpoint and verifies JWT signature.<br/>Checks JWT's exp claim to ensure it's not expired.<br/>If JWT has nbf claim, ensures current time is after it.<br/>Validates JWT's iss claim matches expected issuer (Auth0)<br/>Check aud is https://hello-world.example.com
  SecFChain->>SecFChain: .
  SecFChain-->>Http: handleAuthenticationError
  Note over Http,SecFChain: Status: 401  APPLICATION_JSON<br/>Body: {"message": "Requires authentication"}
  SecFChain->>Router: Access Granted
  Router->>Handler: /protected -> getProtected(ServerRequest)
  Router-->>Http: handleInternalError
  Note over Http,Router: Status: 500 (INTERNAL_SERVER_ERROR)<br/>Content-Type: Determined by ServerResponse<br/>Body: Error message from the thrown error
  Handler->>Service: getProtectedMessage(), returns Message
  Note left of Service: If there was a repo,<br/>this service method<br/>would interact with it
  Service->>Handler: returns Message
  Handler->>RespHeaders: Build response
  Note over Handler, RespHeaders: Status: 200 (OK)<br/>Body: {"text": "This is a protected message."}
  Note over RespHeaders: Set security-related headers: <br/>X-XSS-Protection, Strict-Transport-Security, etc.
  RespHeaders->>Http: Response
  Note over Http, RespHeaders: Status: 200 (OK)<br/>Body: {"text": "This is a protected message."}<br/>Headers: Access-Control-Allow-Origin: http://localhost:4040, Security headers, etc
  Note right of Http: Browser checks for CORS headers.<br/> If missing or incorrect,<br/> browser blocks access<br/> to the response and logs a security error.<br/>Prevents unauthorized sites<br/> from reading responses
```

### Happy path diagram
```mermaid
sequenceDiagram
  participant Http as HTTP/Browser
  participant CorsF as CORS Filter (ApplicationConfig)
  participant SecFChain as Security Filter Chain (SecurityConfig)
  participant Router as Router
  participant RespHeaders as Response Headers Filter
  participant Handler as Handler
  participant Service as Service

  Http->>CorsF: Preflight Request<br/> Could be cached or unnecessary
  Note over Http,CorsF: OPTIONS /api/messages/protected<br/>Origin: http://localhost:4040<br/>Access-Control-Request-Method: GET<br/>Access-Control-Request-Headers: Authorization, Content-Type
  CorsF->>Http: Preflight Response
  Note over CorsF,Http: Status: 200 (OK)<br/>Access-Control-Allow-Origin: http://localhost:4040<br/>Access-Control-Allow-Methods: GET<br/>Access-Control-Allow-Headers: Authorization, Content-Type<br/>Access-Control-Max-Age: 86400
  Http->>SecFChain: Send Actual Request
  Note over Http,SecFChain: GET /api/messages/protected<br/>Authorization: Bearer (JWT access token)
  SecFChain->>SecFChain: Path Exclusion Check for /protected and /admin
  Note over SecFChain: Authentication Check (JWT)<br/>NimbusJwtDecoder automatically:<br/>Fetches public key from JWKS endpoint and verifies JWT signature.<br/>Checks JWT's exp claim to ensure it's not expired.<br/>If JWT has nbf claim, ensures current time is after it.<br/>Validates JWT's iss claim matches expected issuer (Auth0)<br/>Check aud is https://hello-world.example.com
  SecFChain->>SecFChain: .
   SecFChain->>Router: Access Granted
  Router->>Handler: /protected -> getProtected(ServerRequest)
   Handler->>Service: getProtectedMessage(), returns Message
  Note left of Service: If there was a repo,<br/>this service method<br/>would interact with it
  Service->>Handler: returns Message
  Handler->>RespHeaders: Build response
  Note over Handler, RespHeaders: Status: 200 (OK)<br/>Body: {"text": "This is a protected message."}
  Note over RespHeaders: Set security-related headers: <br/>X-XSS-Protection, Strict-Transport-Security, etc.
  RespHeaders->>Http: Response
  Note over Http, RespHeaders: Status: 200 (OK)<br/>Body: {"text": "This is a protected message."}<br/>Headers: Access-Control-Allow-Origin: http://localhost:4040, Security headers, etc
  Note right of Http: Browser sees<br/> Access-Control-Allow-Origin:<br/> http://localhost:4040<br/>and displays content
```

### CORS rejection
```mermaid
sequenceDiagram
  participant Http as HTTP/Browser
  participant CorsF as CORS Filter (ApplicationConfig)
  participant SecFChain as Security Filter Chain (SecurityConfig)
  participant Router as Router
  participant RespHeaders as Response Headers Filter
  participant Handler as Handler
  participant Service as Service

  Http->>CorsF: Preflight Request<br/> Could be cached or unnecessary
  Note over Http,CorsF: OPTIONS /api/messages/protected<br/>Origin: http://localhost:4040<br/>Access-Control-Request-Method: GET<br/>Access-Control-Request-Headers: Authorization, Content-Type
  CorsF->>Http: Preflight Response
  Note over CorsF,Http: 403 Forbidden (Browser doesn't make actual request)
  ```

### Issue with JWT
```mermaid
sequenceDiagram
  participant Http as HTTP/Browser
  participant CorsF as CORS Filter (ApplicationConfig)
  participant SecFChain as Security Filter Chain (SecurityConfig)
  participant Router as Router
  participant RespHeaders as Response Headers Filter
  participant Handler as Handler
  participant Service as Service

  Http->>CorsF: Preflight Request<br/> Could be cached or unnecessary
  Note over Http,CorsF: OPTIONS /api/messages/protected<br/>Origin: http://localhost:4040<br/>Access-Control-Request-Method: GET<br/>Access-Control-Request-Headers: Authorization, Content-Type
  CorsF->>Http: Preflight Response
  Note over CorsF,Http: Status: 200 (OK)<br/>Access-Control-Allow-Origin: http://localhost:4040<br/>Access-Control-Allow-Methods: GET<br/>Access-Control-Allow-Headers: Authorization, Content-Type<br/>Access-Control-Max-Age: 86400
  Http->>SecFChain: Send Actual Request
  Note over Http,SecFChain: GET /api/messages/protected<br/>Authorization: Bearer (JWT access token)
  SecFChain->>SecFChain: Path Exclusion Check for /protected and /admin
  Note over SecFChain: Authentication Check (JWT)<br/>NimbusJwtDecoder automatically:<br/>Fetches public key from JWKS endpoint and verifies JWT signature.<br/>Checks JWT's exp claim to ensure it's not expired.<br/>If JWT has nbf claim, ensures current time is after it.<br/>Validates JWT's iss claim matches expected issuer (Auth0)<br/>Check aud is https://hello-world.example.com
  SecFChain->>SecFChain: .
  SecFChain-->>Http: handleAuthenticationError
  Note over Http,SecFChain: Status: 401  APPLICATION_JSON<br/>Body: {"message": "Requires authentication"}
  ```

### Internal Server Error
```mermaid
sequenceDiagram
  participant Http as HTTP/Browser
  participant CorsF as CORS Filter (ApplicationConfig)
  participant SecFChain as Security Filter Chain (SecurityConfig)
  participant Router as Router
  participant RespHeaders as Response Headers Filter
  participant Handler as Handler
  participant Service as Service

  Http->>CorsF: Preflight Request<br/> Could be cached or unnecessary
  Note over Http,CorsF: OPTIONS /api/messages/protected<br/>Origin: http://localhost:4040<br/>Access-Control-Request-Method: GET<br/>Access-Control-Request-Headers: Authorization, Content-Type
  CorsF->>Http: Preflight Response
  Note over CorsF,Http: Status: 200 (OK)<br/>Access-Control-Allow-Origin: http://localhost:4040<br/>Access-Control-Allow-Methods: GET<br/>Access-Control-Allow-Headers: Authorization, Content-Type<br/>Access-Control-Max-Age: 86400
  Http->>SecFChain: Send Actual Request
  Note over Http,SecFChain: GET /api/messages/protected<br/>Authorization: Bearer (JWT access token)
  SecFChain->>SecFChain: Path Exclusion Check for /protected and /admin
  Note over SecFChain: Authentication Check (JWT)<br/>NimbusJwtDecoder automatically:<br/>Fetches public key from JWKS endpoint and verifies JWT signature.<br/>Checks JWT's exp claim to ensure it's not expired.<br/>If JWT has nbf claim, ensures current time is after it.<br/>Validates JWT's iss claim matches expected issuer (Auth0)<br/>Check aud is https://hello-world.example.com
  SecFChain->>SecFChain: .
  SecFChain->>Router: Access Granted
  Note over Router: ¡ Error occurs in call stack past router !
  Router-->>Http: handleInternalError
  Note over Http,Router: Status: 500 (INTERNAL_SERVER_ERROR)<br/>Content-Type: Determined by ServerResponse<br/>Body: Error message from the thrown error
  ```

### CORS Headers missing/wrong in response
```mermaid
sequenceDiagram
  participant Http as HTTP/Browser
  participant CorsF as CORS Filter (ApplicationConfig)
  participant SecFChain as Security Filter Chain (SecurityConfig)
  participant Router as Router
  participant RespHeaders as Response Headers Filter
  participant Handler as Handler
  participant Service as Service

  Http->>CorsF: Preflight Request<br/> Could be cached or unnecessary
  Note over Http,CorsF: OPTIONS /api/messages/protected<br/>Origin: http://localhost:4040<br/>Access-Control-Request-Method: GET<br/>Access-Control-Request-Headers: Authorization, Content-Type
  CorsF->>Http: Preflight Response
  Note over CorsF,Http: Status: 200 (OK)<br/>Access-Control-Allow-Origin: http://localhost:4040<br/>Access-Control-Allow-Methods: GET<br/>Access-Control-Allow-Headers: Authorization, Content-Type<br/>Access-Control-Max-Age: 86400<br/>
  Http->>SecFChain: Send Actual Request
  Note over Http,SecFChain: GET /api/messages/protected<br/>Authorization: Bearer (JWT access token)
  SecFChain->>SecFChain: Path Exclusion Check for /protected and /admin
  Note over SecFChain: Authentication Check (JWT)<br/>NimbusJwtDecoder automatically:<br/>Fetches public key from JWKS endpoint and verifies JWT signature.<br/>Checks JWT's exp claim to ensure it's not expired.<br/>If JWT has nbf claim, ensures current time is after it.<br/>Validates JWT's iss claim matches expected issuer (Auth0)<br/>Check aud is https://hello-world.example.com
  SecFChain->>SecFChain: .
  SecFChain->>Router: Access Granted
  Router->>Handler: /protected -> getProtected(ServerRequest)
    Handler->>Service: getProtectedMessage(), returns Message
  Note left of Service: If there was a repo,<br/>this service method<br/>would interact with it
  Service->>Handler: returns Message
  Handler->>RespHeaders: Build response
  Note over Handler, RespHeaders: Status: 200 (OK)<br/>Body: {"text": "This is a protected message."}
  Note over RespHeaders: Set security-related headers: <br/>X-XSS-Protection, Strict-Transport-Security, etc.
  RespHeaders->>Http: Response
  Note over Http, RespHeaders: Status: 200 (OK)<br/>Body: {"text": "This is a protected message."}<br/>Headers: Access-Control-Allow-Origin: http://wrongOrMissing, Security headers, etc
  Note right of Http: Browser checks for CORS headers.<br/> Missing or incorrect, so<br/> browser blocks access<br/> to the response and logs a security error.<br/>Prevents unauthorized sites<br/> from reading responses
```
