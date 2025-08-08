# Technical Ideas Extracted from Javalin Framework

This document outlines the key technical concepts and architectural patterns identified in the Javalin web framework and how they are demonstrated in the Mini-Javalin proof of concept.

## Core Technical Ideas

### 1. Fluent API Design Pattern
**Concept**: Method chaining allows creating readable, domain-specific languages (DSL)
- **Javalin**: `Javalin.create().get("/", handler).start(7070)`
- **Mini-Javalin**: `MiniJavalin.create().get("/", handler).start(8080)`
- **Implementation**: Each method returns `this` to enable chaining

### 2. Functional Interface-Based Handler System
**Concept**: Simple functional interfaces without annotations or reflection
- **Javalin**: `Handler` interface with single `handle(Context ctx)` method
- **Mini-Javalin**: Identical pattern - `@FunctionalInterface Handler`
- **Benefits**: No magic, no annotations, just clean functional code

### 3. Context-Based Request/Response Abstraction
**Concept**: Unified API that wraps underlying HTTP implementation details
- **Javalin**: `Context` wraps HttpServletRequest/HttpServletResponse
- **Mini-Javalin**: `Context` wraps `HttpExchange` from Java's built-in HTTP server
- **Features**: 
  - Request access: `path()`, `method()`, `queryParam()`, `pathParam()`, `body()`
  - Response building: `result()`, `json()`, `status()`, `contentType()`

### 4. Path Matching and Routing
**Concept**: Efficient path matching with parameter extraction
- **Pattern**: Convert paths like `/users/{id}` to regex patterns
- **Implementation**: `Router` class with `Route` objects containing compiled patterns
- **Features**: Path parameters, wildcard matching, method-specific routing

### 5. Embedded Server Architecture
**Concept**: Framework controls the server lifecycle
- **Javalin**: Uses embedded Jetty server
- **Mini-Javalin**: Uses Java's built-in `HttpServer`
- **Benefits**: Easy startup/shutdown, minimal configuration, self-contained

### 6. Before/After Filter System
**Concept**: Middleware-like cross-cutting concerns
- **Use Cases**: Logging, authentication, CORS, request timing
- **Implementation**: Lists of handlers executed before/after main route handlers
- **Pattern**: Same `Handler` interface for consistency

### 7. Centralized Error/Exception Handling
**Concept**: Global exception handling with type-specific handlers
- **Pattern**: Map exception types to handler functions
- **Implementation**: `Map<Class<? extends Exception>, ExceptionHandler>`
- **Benefits**: Consistent error handling, separation of concerns

### 8. Layered Architecture
**Concept**: Clean separation of concerns across layers
- **Layers**: 
  - API Layer (MiniJavalin - fluent interface)
  - Routing Layer (Router - path matching)
  - Context Layer (Context - request/response abstraction) 
  - Server Layer (embedded HTTP server)

## Architecture Comparison

| Aspect | Javalin (Production) | Mini-Javalin (POC) |
|--------|---------------------|-------------------|
| Server | Embedded Jetty | Java HttpServer |
| Size | ~318 files | ~5 files |
| Features | Full production features | Core concepts only |
| Dependencies | Multiple | Zero (pure Java) |
| Lines of Code | ~50K+ | ~500 |

## Key Design Principles Demonstrated

1. **Simplicity**: No annotations, no reflection, no magic
2. **Fluency**: Readable DSL through method chaining
3. **Functional**: Functional interfaces and lambda-friendly design
4. **Extensible**: Plugin-like architecture with filters and exception handlers
5. **Self-contained**: Embedded server, minimal dependencies
6. **Type-safe**: Strong typing throughout the API

## Running the Demo

```bash
cd mini-javalin-poc
mvn compile exec:java -Dexec.mainClass="minijavalin.Example"
```

This will start the server on port 8080 and demonstrate all the key concepts working together.