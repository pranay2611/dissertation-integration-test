package com.dissertation.integrationtestautomation.utils;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for test data creation and helper methods
 */
public class TestDataUtils {

    /**
     * Register a user and return the JWT token
     * If registration fails (e.g., user already exists), attempts to login instead
     *
     * @param username the username
     * @param email the email address
     * @return JWT token or null if both registration and login fail
     */
    public static String registerAndGetToken(String username, String email) {
        try {
            Response response = ApiClient.registerUser(username, email, "password123", "USER");
            
            if (response == null) {
                System.err.println("ERROR: Registration response is null. Check network connectivity and user service availability.");
                return null;
            }
            
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody() != null ? response.getBody().asString() : "null";
            
            System.out.println("registerAndGetToken - Registration Status: " + statusCode + ", Body: " + responseBody);
            System.out.println("registerAndGetToken - Response Headers: " + response.getHeaders());
            
            if (statusCode == 200) {
                try {
                    String token = response.jsonPath().get("token");
                    if (token != null && !token.trim().isEmpty()) {
                        System.out.println("registerAndGetToken - Token extracted successfully");
                        return token;
                    } else {
                        System.err.println("ERROR: Token is null or empty in registration response. Full response: " + responseBody);
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to parse token from registration response: " + e.getMessage());
                    System.err.println("Response body: " + responseBody);
                    e.printStackTrace();
                }
            } else {
                System.out.println("Registration failed with status: " + statusCode + ". Attempting login...");
                // Try to login if registration failed (user might already exist)
                try {
                    Response loginResponse = ApiClient.loginUser(username, "password123");
                    
                    if (loginResponse == null) {
                        System.err.println("ERROR: Login response is null. Check network connectivity and user service availability.");
                        return null;
                    }
                    
                    int loginStatusCode = loginResponse.getStatusCode();
                    String loginResponseBody = loginResponse.getBody() != null ? loginResponse.getBody().asString() : "null";
                    
                    System.out.println("registerAndGetToken - Login Status: " + loginStatusCode + ", Body: " + loginResponseBody);
                    
                    if (loginStatusCode == 200) {
                        try {
                            String token = loginResponse.jsonPath().get("token");
                            if (token != null && !token.trim().isEmpty()) {
                                System.out.println("Login successful, token obtained");
                                return token;
                            } else {
                                System.err.println("ERROR: Token is null or empty in login response. Full response: " + loginResponseBody);
                            }
                        } catch (Exception e) {
                            System.err.println("ERROR: Failed to parse token from login response: " + e.getMessage());
                            System.err.println("Response body: " + loginResponseBody);
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("ERROR: Both registration and login failed. Registration: " + statusCode + 
                                " (" + responseBody + "), Login: " + loginStatusCode + " (" + loginResponseBody + ")");
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: Exception during login attempt: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Exception during registration attempt: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Register a user and return the JWT token with custom password
     *
     * @param username the username
     * @param email the email address
     * @param password the password
     * @return JWT token or null if registration fails
     */
    public static String registerAndGetToken(String username, String email, String password) {
        Response response = ApiClient.registerUser(username, email, password, "USER");
        
        if (response.getStatusCode() == 200) {
            return response.jsonPath().get("token");
        }
        return null;
    }

    /**
     * Create an order and return the order number
     *
     * @param token the JWT token
     * @param username the username
     * @param productName the product name
     * @param quantity the quantity
     * @param unitPrice the unit price
     * @return order number or null if order creation fails
     */
    public static String createOrderAndGetOrderNumber(String token, String username, 
                                                      String productName, int quantity, double unitPrice) {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("ERROR: Token is null or empty in createOrderAndGetOrderNumber");
            return null;
        }
        
        Response response = ApiClient.createOrder(username, productName, quantity, unitPrice, token);
        
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asString();
        
        System.out.println("createOrderAndGetOrderNumber - Status: " + statusCode + ", Body: " + responseBody);
        
        if (statusCode == 201 || statusCode == 200) {
            if (responseBody == null || responseBody.trim().isEmpty()) {
                System.err.println("ERROR: Response body is empty for successful order creation");
                return null;
            }
            
            try {
                String orderNumber = response.jsonPath().get("orderNumber");
                if (orderNumber == null || orderNumber.trim().isEmpty()) {
                    System.err.println("ERROR: orderNumber is null or empty in response. Full response: " + responseBody);
                    return null;
                }
                return orderNumber;
            } catch (Exception e) {
                System.err.println("ERROR: Failed to parse orderNumber from response: " + e.getMessage());
                System.err.println("Response body: " + responseBody);
                return null;
            }
        } else {
            System.err.println("ERROR: Order creation failed with status: " + statusCode + ", Response: " + responseBody);
            return null;
        }
    }

    /**
     * Create a user registration request body
     *
     * @param username the username
     * @param email the email address
     * @param password the password
     * @param role the user role
     * @return Map containing request body
     */
    public static Map<String, Object> createUserRegistrationBody(String username, String email, 
                                                                 String password, String role) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("role", role);
        return requestBody;
    }

    /**
     * Create a user login request body
     *
     * @param username the username
     * @param password the password
     * @return Map containing request body
     */
    public static Map<String, Object> createUserLoginBody(String username, String password) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        return requestBody;
    }

    /**
     * Create an order request body
     *
     * @param username the username
     * @param productName the product name
     * @param quantity the quantity
     * @param unitPrice the unit price
     * @return Map containing request body
     */
    public static Map<String, Object> createOrderBody(String username, String productName, 
                                                      int quantity, double unitPrice) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("productName", productName);
        requestBody.put("quantity", quantity);
        requestBody.put("unitPrice", unitPrice);
        return requestBody;
    }

    /**
     * Wait for a specified number of milliseconds
     * Useful for waiting for async operations like notifications
     *
     * @param milliseconds the number of milliseconds to wait
     */
    public static void waitForAsyncOperation(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Generate a unique username that complies with validation rules (3-20 characters)
     * Format: prefix + last 6 digits of timestamp + last 3 digits of random
     * 
     * @param prefix the prefix for the username (e.g., "notif", "order", "user")
     * @return a valid username between 3 and 20 characters
     */
    public static String generateValidUsername(String prefix) {
        long timestamp = System.currentTimeMillis();
        int randomSuffix = (int)(Math.random() * 10000);
        
        // Use last 6 digits of timestamp and last 3 digits of random to keep it short
        String timestampStr = String.valueOf(timestamp % 1000000); // Last 6 digits
        String randomStr = String.valueOf(randomSuffix % 1000); // Last 3 digits
        
        // Ensure prefix is not too long
        if (prefix == null || prefix.isEmpty()) {
            prefix = "u";
        }
        if (prefix.length() > 10) {
            prefix = prefix.substring(0, 10);
        }
        
        String username = prefix + timestampStr + randomStr;
        
        // Ensure it's between 3 and 20 characters
        if (username.length() > 20) {
            username = username.substring(0, 20);
        } else if (username.length() < 3) {
            username = username + "123"; // Pad if too short
        }
        
        return username;
    }
}

