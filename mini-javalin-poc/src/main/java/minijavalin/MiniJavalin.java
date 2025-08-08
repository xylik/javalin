package minijavalin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Main framework class implementing the fluent API pattern.
 * Core concepts: Fluent API, embedded server, before/after filters, error handling.
 */
public class MiniJavalin {
    private final Router router = new Router();
    private final List<Handler> beforeFilters = new ArrayList<>();
    private final List<Handler> afterFilters = new ArrayList<>();
    private final Map<Class<? extends Exception>, ExceptionHandler> exceptionHandlers = new HashMap<>();
    private HttpServer server;
    
    private MiniJavalin() {}
    
    public static MiniJavalin create() {
        return new MiniJavalin();
    }
    
    // HTTP method handlers - fluent API pattern
    public MiniJavalin get(String path, Handler handler) {
        router.addRoute("GET", path, handler);
        return this;
    }
    
    public MiniJavalin post(String path, Handler handler) {
        router.addRoute("POST", path, handler);
        return this;
    }
    
    public MiniJavalin put(String path, Handler handler) {
        router.addRoute("PUT", path, handler);
        return this;
    }
    
    public MiniJavalin delete(String path, Handler handler) {
        router.addRoute("DELETE", path, handler);
        return this;
    }
    
    // Filter support - before/after processing
    public MiniJavalin before(Handler filter) {
        beforeFilters.add(filter);
        return this;
    }
    
    public MiniJavalin after(Handler filter) {
        afterFilters.add(filter);
        return this;
    }
    
    // Exception handling
    public <T extends Exception> MiniJavalin exception(Class<T> exceptionClass, ExceptionHandler<T> handler) {
        exceptionHandlers.put(exceptionClass, handler);
        return this;
    }
    
    // Server lifecycle
    public MiniJavalin start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new RequestHandler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("Mini-Javalin server started on port " + port);
            return this;
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server", e);
        }
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Mini-Javalin server stopped");
        }
    }
    
    @FunctionalInterface
    public interface ExceptionHandler<T extends Exception> {
        void handle(T exception, Context ctx);
    }
    
    private class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Context ctx = new Context(exchange);
            
            try {
                // Execute before filters
                for (Handler filter : beforeFilters) {
                    filter.handle(ctx);
                }
                
                // Find and execute route handler
                Router.Route route = router.findRoute(ctx.method(), ctx.path());
                if (route != null) {
                    // Set path parameters
                    Map<String, String> pathParams = route.extractPathParams(ctx.path());
                    ctx.setPathParams(pathParams);
                    
                    // Execute handler
                    route.handler.handle(ctx);
                } else {
                    // 404 Not Found
                    ctx.status(404).result("Not Found: " + ctx.path());
                }
                
                // Execute after filters
                for (Handler filter : afterFilters) {
                    filter.handle(ctx);
                }
                
            } catch (Exception e) {
                handleException(e, ctx);
            }
            
            // Send response
            sendResponse(exchange, ctx);
        }
        
        private void handleException(Exception e, Context ctx) {
            ExceptionHandler handler = exceptionHandlers.get(e.getClass());
            if (handler != null) {
                try {
                    handler.handle(e, ctx);
                } catch (Exception handlerException) {
                    // Fallback error handling
                    ctx.status(500).result("Internal Server Error: " + handlerException.getMessage());
                }
            } else {
                // Default error handling
                ctx.status(500).result("Internal Server Error: " + e.getMessage());
            }
        }
        
        private void sendResponse(HttpExchange exchange, Context ctx) throws IOException {
            String response = ctx.getResponseBody();
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            
            exchange.sendResponseHeaders(ctx.status(), responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}