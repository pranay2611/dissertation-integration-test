package com.dissertation.integrationtestautomation.utils;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * High-level API client for microservices endpoints
 * Provides business-level methods for common operations
 */
public class ApiClient {

    // Option to bypass gateway for integration tests (call services directly)
    // Set this system property to "true" to bypass gateway: -Dbypass.gateway=true
    private static final boolean BYPASS_GATEWAY = Boolean.parseBoolean(System.getProperty("bypass.gateway", "false"));
    
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String USER_SERVICE_URL = "http://localhost:8081";
    private static final String ORDER_SERVICE_URL = "http://localhost:8082";
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8083";
    private static final String NOTIFICATION_SERVICE_URL = "http://localhost:8084";
    
    private static final String BASE_URL = BYPASS_GATEWAY ? "" : GATEWAY_URL;
    private static final String AUTH_BASE = BYPASS_GATEWAY ? USER_SERVICE_URL + "/api/auth" : BASE_URL + "/api/auth";
    private static final String ORDERS_BASE = BYPASS_GATEWAY ? ORDER_SERVICE_URL + "/api/orders" : BASE_URL + "/api/orders";
    private static final String PAYMENTS_BASE = BYPASS_GATEWAY ? PAYMENT_SERVICE_URL + "/api/payments" : BASE_URL + "/api/payments";
    private static final String NOTIFICATIONS_BASE = BYPASS_GATEWAY ? NOTIFICATION_SERVICE_URL + "/api/notifications" : BASE_URL + "/api/notifications";

    /**
     * Register a new user
     *
     * @param username the username
     * @param email the email address
     * @param password the password
     * @param role the user role (USER, ADMIN, etc.)
     * @return Response object
     */
    public static Response registerUser(String username, String email, String password, String role) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("role", role);

        return RestApiUtils.postRequest(AUTH_BASE + "/register", requestBody);
    }

    /**
     * Register a user with default role USER
     *
     * @param username the username
     * @param email the email address
     * @param password the password
     * @return Response object
     */
    public static Response registerUser(String username, String email, String password) {
        return registerUser(username, email, password, "USER");
    }

    /**
     * Login a user
     *
     * @param username the username
     * @param password the password
     * @return Response object
     */
    public static Response loginUser(String username, String password) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        return RestApiUtils.postRequest(AUTH_BASE + "/login", requestBody);
    }

    /**
     * Get user details
     *
     * @param username the username
     * @param token the JWT token
     * @return Response object
     */
    public static Response getUserDetails(String username, String token) {
        return RestApiUtils.getRequestWithAuth(AUTH_BASE + "/user/" + username, token);
    }

    /**
     * Create an order
     *
     * @param username the username
     * @param productName the product name
     * @param quantity the quantity
     * @param unitPrice the unit price
     * @param token the JWT token
     * @return Response object
     */
    public static Response createOrder(String username, String productName, int quantity, 
                                       double unitPrice, String token) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("productName", productName);
        requestBody.put("quantity", quantity);
        requestBody.put("unitPrice", unitPrice);

        return RestApiUtils.postRequestWithAuth(ORDERS_BASE, requestBody, token);
    }

    /**
     * Get order details by order number
     *
     * @param orderNumber the order number
     * @param token the JWT token
     * @return Response object
     */
    public static Response getOrderDetails(String orderNumber, String token) {
        return RestApiUtils.getRequestWithAuth(ORDERS_BASE + "/" + orderNumber, token);
    }

    /**
     * Get all orders for a user
     *
     * @param username the username
     * @param token the JWT token (can be null for testing auth failures)
     * @return Response object
     */
    public static Response getUserOrders(String username, String token) {
        if (token == null) {
            return RestApiUtils.getRequest(ORDERS_BASE + "/user/" + username);
        }
        return RestApiUtils.getRequestWithAuth(ORDERS_BASE + "/user/" + username, token);
    }

    /**
     * Get payment details for an order
     *
     * @param orderNumber the order number
     * @param token the JWT token
     * @return Response object
     */
    public static Response getPaymentDetails(String orderNumber, String token) {
        return RestApiUtils.getRequestWithAuth(PAYMENTS_BASE + "/order/" + orderNumber, token);
    }

    /**
     * Get notifications for a user
     *
     * @param username the username
     * @param token the JWT token
     * @return Response object
     */
    public static Response getUserNotifications(String username, String token) {
        return RestApiUtils.getRequestWithAuth(NOTIFICATIONS_BASE + "/user/" + username, token);
    }

    /**
     * Get user service health status
     *
     * @return Response object
     */
    public static Response getUserServiceHealth() {
        return RestApiUtils.getRequest(AUTH_BASE + "/health");
    }
}

