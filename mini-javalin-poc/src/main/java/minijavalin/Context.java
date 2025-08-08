package minijavalin;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Context abstraction for request/response handling.
 * Core concept: Unified API that wraps the underlying HTTP implementation.
 */
public class Context {
    private final HttpExchange exchange;
    private Map<String, String> pathParams = new HashMap<>();
    private int statusCode = 200;
    private String responseBody = "";
    
    public Context(HttpExchange exchange) {
        this.exchange = exchange;
    }
    
    // Request methods
    public String method() {
        return exchange.getRequestMethod();
    }
    
    public String path() {
        return exchange.getRequestURI().getPath();
    }
    
    public String queryParam(String name) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(name)) {
                try {
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }
    
    public String pathParam(String name) {
        return pathParams.get(name);
    }
    
    public String body() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        } catch (IOException e) {
            return "";
        }
    }
    
    public String header(String name) {
        List<String> headers = exchange.getRequestHeaders().get(name);
        return headers != null && !headers.isEmpty() ? headers.get(0) : null;
    }
    
    // Response methods
    public Context result(String content) {
        this.responseBody = content;
        return this;
    }
    
    public Context json(Object object) {
        // Simple JSON serialization (in real implementation would use proper JSON library)
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
            json.append("}");
            this.responseBody = json.toString();
        } else {
            this.responseBody = "\"" + object.toString() + "\"";
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        return this;
    }
    
    public Context status(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }
    
    public int status() {
        return statusCode;
    }
    
    public Context contentType(String contentType) {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        return this;
    }
    
    // Internal methods
    void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }
    
    HttpExchange getExchange() {
        return exchange;
    }
    
    String getResponseBody() {
        return responseBody;
    }
}