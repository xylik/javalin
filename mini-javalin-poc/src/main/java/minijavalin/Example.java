package minijavalin;

import java.util.Map;

/**
 * Example usage demonstrating all the key concepts extracted from Javalin.
 */
public class Example {
    
    public static void main(String[] args) {
        // Demonstrate fluent API, functional handlers, routing, filters, and error handling
        MiniJavalin app = MiniJavalin.create()
            // Before filter - cross-cutting concern
            .before(ctx -> {
                System.out.println("Before: " + ctx.method() + " " + ctx.path());
                ctx.contentType("text/plain; charset=utf-8");
            })
            
            // Basic route
            .get("/", ctx -> ctx.result("Hello Mini-Javalin!"))
            
            // Route with path parameter
            .get("/users/{id}", ctx -> {
                String id = ctx.pathParam("id");
                ctx.json(Map.of("userId", id, "name", "User " + id));
            })
            
            // Route with query parameters
            .get("/search", ctx -> {
                String query = ctx.queryParam("q");
                if (query == null) {
                    ctx.status(400).result("Missing query parameter 'q'");
                } else {
                    ctx.result("Searching for: " + query);
                }
            })
            
            // POST route demonstrating body handling
            .post("/users", ctx -> {
                String body = ctx.body();
                ctx.status(201).result("Created user: " + body);
            })
            
            // Route that throws an exception to demonstrate error handling
            .get("/error", ctx -> {
                throw new RuntimeException("Intentional error for demonstration");
            })
            
            // Exception handler
            .exception(RuntimeException.class, (e, ctx) -> {
                ctx.status(500).result("Caught exception: " + e.getMessage());
            })
            
            // After filter - logging, cleanup, etc.
            .after(ctx -> {
                System.out.println("After: " + ctx.status() + " " + ctx.path());
            })
            
            // Start the server
            .start(8080);
        
        System.out.println("\nTry these URLs:");
        System.out.println("  http://localhost:8080/");
        System.out.println("  http://localhost:8080/users/123");
        System.out.println("  http://localhost:8080/search?q=hello");
        System.out.println("  http://localhost:8080/error");
        System.out.println("\nPress Ctrl+C to stop the server");
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }
}