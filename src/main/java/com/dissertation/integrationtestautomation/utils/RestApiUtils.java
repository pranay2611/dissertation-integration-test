package com.dissertation.integrationtestautomation.utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Utility class for REST API calls using RestAssured
 * Provides reusable methods for common HTTP operations
 */
public class RestApiUtils {

    // Configure RestAssured to use HTTP/1.1 (matching curl's behavior)
    // RestAssured 5.x uses Apache HttpClient 5, which may default to HTTP/2 for HTTPS
    // We force HTTP/1.1 by setting system properties
    static {
        // Force HTTP/1.1 protocol version
        System.setProperty("http.protocol.version", "HTTP/1.1");
        // Disable HTTP/2 for Apache HttpClient
        System.setProperty("httpclient.protocol.version", "HTTP/1.1");
    }

    /** Max retries for POST when gateway returns 405 (Method Not Allowed) or 503 (Service Unavailable). */
    private static final int POST_RETRY_MAX = 3;
    /** Delay in ms between retries. */
    private static final int POST_RETRY_DELAY_MS = 500;

    /**
     * Perform a POST request with JSON body.
     * Retries on 405 (Method Not Allowed) or 503 (Service Unavailable) to avoid transient gateway failures.
     *
     * @param endpoint the API endpoint
     * @param requestBody the request body as Map
     * @return Response object
     */
    public static Response postRequest(String endpoint, Map<String, Object> requestBody) {
        // Match curl's exact request format
        // curl sends minimal headers: Host, User-Agent, Accept: */*, Content-Type: application/json
        // Key: curl doesn't send Origin, Referer, or other CORS-triggering headers
        // CRITICAL: Disable cookies to avoid CSRF token issues - curl doesn't send cookies
        // Cookies can trigger Spring Security CSRF checks even when CSRF is disabled

        Response lastResponse = null;
        int attempt = 0;

        while (attempt < POST_RETRY_MAX) {
            attempt++;
            if (attempt > 1) {
                System.out.println("postRequest - Retry attempt " + attempt + "/" + POST_RETRY_MAX + " for " + endpoint);
            }
            System.out.println("===========================================");
            System.out.println("RestAssured POST Request Details:");
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Request Body: " + requestBody);
            System.out.println("===========================================");

            try {
                Response response = doPostRequest(endpoint, requestBody);

                if (response == null) {
                    System.err.println("ERROR: Response is null from postRequest for endpoint: " + endpoint);
                    throw new RuntimeException("Response is null from POST request to " + endpoint);
                }

                int status = response.getStatusCode();
                System.out.println("postRequest - Response Status: " + status +
                        ", Body: " + (response.getBody() != null ? response.getBody().asString() : "null"));

                // Retry on transient 405 or 503 (gateway route/circuit breaker issues under load)
                if ((status == 405 || status == 503) && attempt < POST_RETRY_MAX) {
                    System.out.println("postRequest - Transient " + status + ", retrying in " + POST_RETRY_DELAY_MS + "ms...");
                    try {
                        Thread.sleep(POST_RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return response;
                    }
                    lastResponse = response;
                    continue;
                }

                return response;
            } catch (Exception e) {
                if (attempt >= POST_RETRY_MAX) {
                    System.err.println("ERROR: Exception in postRequest for endpoint: " + endpoint);
                    System.err.println("Exception type: " + e.getClass().getName());
                    System.err.println("Exception message: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Failed to execute POST request to " + endpoint + ": " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(POST_RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Failed to execute POST request to " + endpoint + ": " + e.getMessage(), e);
                }
            }
        }

        return lastResponse != null ? lastResponse : doPostRequest(endpoint, requestBody);
    }

    /**
     * Single POST attempt (no retry). Used by postRequest and postRequestWithAuth.
     */
    private static Response doPostRequest(String endpoint, Map<String, Object> requestBody) {
        return given()
                .log().all()
                .cookies(new java.util.HashMap<>())
                .contentType(ContentType.JSON)
                .accept("*/*")
                .header("User-Agent", "curl/8.4.0")
                .header("Connection", "close")
                .redirects().follow(false)
                .body(requestBody)
                .when()
                .post(endpoint)
                .then()
                .log().all()
                .extract()
                .response();
    }

    /**
     * Perform a POST request with JSON body and Authorization header
     *
     * @param endpoint the API endpoint
     * @param requestBody the request body as Map
     * @param token the JWT token for authorization
     * @return Response object
     */
    public static Response postRequestWithAuth(String endpoint, Map<String, Object> requestBody, String token) {
        System.out.println("===========================================");
        System.out.println("RestAssured POST Request with Auth Details:");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Request Body: " + requestBody);
        System.out.println("Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
        System.out.println("===========================================");
        
        return given()
                .log().all()  // Log all request details including headers
                .cookies(new java.util.HashMap<>())  // Clear any cookies
                .contentType(ContentType.JSON)
                .accept("*/*")  // Match curl's Accept: */*
                .header("User-Agent", "curl/8.4.0")  // Match curl's User-Agent
                .header("Connection", "close")  // Match curl's default connection behavior
                .header("Authorization", "Bearer " + token)
                .redirects().follow(false)  // curl doesn't follow redirects by default
                .body(requestBody)
                .when()
                .post(endpoint)
                .then()
                .log().all()  // Log all response details including headers
                .extract()
                .response();
    }

    /**
     * Perform a GET request
     *
     * @param endpoint the API endpoint
     * @return Response object
     */
    public static Response getRequest(String endpoint) {
        return given()
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    /**
     * Perform a GET request with Authorization header
     *
     * @param endpoint the API endpoint
     * @param token the JWT token for authorization
     * @return Response object
     */
    public static Response getRequestWithAuth(String endpoint, String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty for authenticated request");
        }
        
        return given()
                .log().all()  // Log all request details
                .cookies(new java.util.HashMap<>())  // Clear any cookies
                .accept("*/*")
                .header("User-Agent", "curl/8.4.0")
                .header("Connection", "close")
                .header("Authorization", "Bearer " + token)
                .redirects().follow(false)
                .when()
                .get(endpoint)
                .then()
                .log().all()  // Log all response details
                .extract()
                .response();
    }

    /**
     * Perform a PUT request with JSON body and Authorization header
     *
     * @param endpoint the API endpoint
     * @param requestBody the request body as Map
     * @param token the JWT token for authorization
     * @return Response object
     */
    public static Response putRequestWithAuth(String endpoint, Map<String, Object> requestBody, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
    }

    /**
     * Perform a DELETE request with Authorization header
     *
     * @param endpoint the API endpoint
     * @param token the JWT token for authorization
     * @return Response object
     */
    public static Response deleteRequestWithAuth(String endpoint, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
    }
}

