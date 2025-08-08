package minijavalin;

/**
 * Functional interface for handling HTTP requests.
 * Core concept: No annotations, no reflection, just simple functional interfaces.
 */
@FunctionalInterface
public interface Handler {
    /**
     * Handle the HTTP request using the provided context.
     * @param ctx the request/response context
     * @throws Exception if an error occurs during handling
     */
    void handle(Context ctx) throws Exception;
}