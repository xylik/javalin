package minijavalin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple routing system with path parameter support.
 * Core concept: Efficient path matching without reflection or annotations.
 */
public class Router {
    private final List<Route> routes = new ArrayList<>();
    
    public static class Route {
        final String method;
        final String path;
        final Pattern pathPattern;
        final List<String> pathParamNames;
        final Handler handler;
        
        public Route(String method, String path, Handler handler) {
            this.method = method;
            this.path = path;
            this.handler = handler;
            this.pathParamNames = new ArrayList<>();
            this.pathPattern = createPathPattern(path);
        }
        
        private Pattern createPathPattern(String path) {
            // Convert path like "/users/{id}" to regex pattern
            String regex = path;
            Pattern paramPattern = Pattern.compile("\\{([^}]+)}");
            Matcher matcher = paramPattern.matcher(path);
            
            while (matcher.find()) {
                String paramName = matcher.group(1);
                pathParamNames.add(paramName);
                regex = regex.replace("{" + paramName + "}", "([^/]+)");
            }
            
            return Pattern.compile("^" + regex + "$");
        }
        
        public boolean matches(String method, String path) {
            return this.method.equals(method) && pathPattern.matcher(path).matches();
        }
        
        public Map<String, String> extractPathParams(String path) {
            Map<String, String> params = new HashMap<>();
            Matcher matcher = pathPattern.matcher(path);
            
            if (matcher.matches()) {
                for (int i = 0; i < pathParamNames.size(); i++) {
                    params.put(pathParamNames.get(i), matcher.group(i + 1));
                }
            }
            
            return params;
        }
    }
    
    public void addRoute(String method, String path, Handler handler) {
        routes.add(new Route(method, path, handler));
    }
    
    public Route findRoute(String method, String path) {
        return routes.stream()
                .filter(route -> route.matches(method, path))
                .findFirst()
                .orElse(null);
    }
}