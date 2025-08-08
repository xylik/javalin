# Mini-Javalin: Proof of Concept Framework

This is a minimal proof of concept framework that demonstrates the key technical ideas implemented in the Javalin web framework.

## Key Technical Ideas Demonstrated

1. **Fluent API Design Pattern** - Method chaining for readable DSL
2. **Functional Interface-Based Handlers** - Simple, no-magic handler system  
3. **Context-Based Request/Response Abstraction** - Unified API wrapper
4. **Path Matching and Routing** - Simple but effective routing system
5. **Embedded Server** - Easy startup/shutdown with minimal configuration
6. **Before/After Filters** - Middleware-like cross-cutting concerns
7. **Error Handling** - Centralized exception and error handling
8. **JSON Integration** - Simple JSON serialization support

## Core Architecture

The framework consists of these core components:

- `MiniJavalin` - Main framework class with fluent API
- `Handler` - Functional interface for request handlers
- `Context` - Request/response abstraction
- `Router` - Path matching and route management
- `Server` - Embedded HTTP server wrapper
- `Filter` - Before/after request processing

## Example Usage

```java
public class Example {
    public static void main(String[] args) {
        MiniJavalin app = MiniJavalin.create()
            .before(ctx -> System.out.println("Before: " + ctx.path()))
            .get("/", ctx -> ctx.result("Hello World!"))
            .get("/users/{id}", ctx -> ctx.json(Map.of("id", ctx.pathParam("id"))))
            .after(ctx -> System.out.println("After: " + ctx.status()))
            .start(8080);
    }
}
```

This demonstrates the same core concepts as Javalin but in a much smaller, educational implementation.