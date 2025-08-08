package minijavalin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Simple integration tests for Mini-Javalin demonstrating all key concepts.
 */
public class MiniJavalinTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Running Mini-Javalin Integration Tests...");
        
        // Start the server
        MiniJavalin app = MiniJavalin.create()
            .before(ctx -> ctx.contentType("text/plain"))
            .get("/", ctx -> ctx.result("Hello Test!"))
            .get("/users/{id}", ctx -> ctx.json(Map.of("id", ctx.pathParam("id"))))
            .get("/query", ctx -> ctx.result("Query: " + ctx.queryParam("q")))
            .post("/echo", ctx -> ctx.result("Echo: " + ctx.body()))
            .exception(RuntimeException.class, (e, ctx) -> ctx.status(500).result("Error: " + e.getMessage()))
            .after(ctx -> System.out.println("Request processed: " + ctx.status()))
            .start(8081);
        
        // Give server time to start
        Thread.sleep(500);
        
        HttpClient client = HttpClient.newHttpClient();
        boolean allTestsPassed = true;
        
        // Test 1: Basic GET
        allTestsPassed &= testEndpoint(client, "GET /", "GET", "http://localhost:8081/", "Hello Test!");
        
        // Test 2: Path parameters
        allTestsPassed &= testEndpoint(client, "GET with path param", "GET", "http://localhost:8081/users/42", "{\"id\":\"42\"}");
        
        // Test 3: Query parameters
        allTestsPassed &= testEndpoint(client, "GET with query param", "GET", "http://localhost:8081/query?q=test", "Query: test");
        
        // Test 4: POST with body
        allTestsPassed &= testPostEndpoint(client, "POST with body", "http://localhost:8081/echo", "test data", "Echo: test data");
        
        // Test 5: 404 handling
        HttpResponse<String> notFoundResponse = client.send(
            HttpRequest.newBuilder().uri(URI.create("http://localhost:8081/notfound")).build(),
            HttpResponse.BodyHandlers.ofString()
        );
        boolean test404 = notFoundResponse.statusCode() == 404 && notFoundResponse.body().contains("Not Found");
        System.out.println("Test 404 handling: " + (test404 ? "PASS" : "FAIL"));
        allTestsPassed &= test404;
        
        app.stop();
        
        System.out.println("\n" + (allTestsPassed ? "All tests PASSED!" : "Some tests FAILED!"));
        System.out.println("\nKey concepts demonstrated:");
        System.out.println("✓ Fluent API design");
        System.out.println("✓ Functional handlers");
        System.out.println("✓ Context abstraction");
        System.out.println("✓ Path and query parameters");
        System.out.println("✓ Before/after filters");
        System.out.println("✓ Error handling");
        System.out.println("✓ Embedded server");
    }
    
    private static boolean testEndpoint(HttpClient client, String testName, String method, String url, String expectedBody) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        
        boolean passed = response.statusCode() == 200 && response.body().equals(expectedBody);
        System.out.println("Test " + testName + ": " + (passed ? "PASS" : "FAIL"));
        if (!passed) {
            System.out.println("  Expected: " + expectedBody);
            System.out.println("  Got: " + response.body());
        }
        return passed;
    }
    
    private static boolean testPostEndpoint(HttpClient client, String testName, String url, String body, String expectedBody) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        
        boolean passed = response.statusCode() == 200 && response.body().equals(expectedBody);
        System.out.println("Test " + testName + ": " + (passed ? "PASS" : "FAIL"));
        if (!passed) {
            System.out.println("  Expected: " + expectedBody);
            System.out.println("  Got: " + response.body());
        }
        return passed;
    }
}