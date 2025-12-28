# Integration Test Automation

This project contains integration tests for the microservices dissertation project using RestAssured and TestNG.

## Technologies

- **Java 17**: Programming language
- **Maven**: Build and dependency management
- **RestAssured**: REST API testing framework
- **TestNG**: Test management and execution framework
- **Jackson**: JSON processing

## Project Structure

```
integrationtestautomation/
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── dissertation/
│       │           └── integrationtestautomation/
│       │               └── tests/
│       │                   └── MicroservicesIntegrationTest.java
│       └── resources/
│           └── testng.xml
├── pom.xml
└── README.md
```

## Prerequisites

1. Java 17 or higher
2. Maven 3.6 or higher
3. Microservices running on localhost:8080 (API Gateway)

## Setup

1. Ensure all microservices are running:
   ```bash
   cd /path/to/dissertation
   docker-compose up -d
   ```

2. Wait for services to be ready (30-60 seconds)

3. Verify API Gateway is accessible:
   ```bash
   curl http://localhost:8080/api/auth/health
   ```

## Running Tests

### Run all tests using Maven

```bash
mvn clean test
```

### Run tests with TestNG XML

```bash
mvn test -DsuiteXmlFile=src/test/resources/testng.xml
```

### Run specific test class

```bash
mvn test -Dtest=MicroservicesIntegrationTest
```

### Run tests with specific test method

```bash
mvn test -Dtest=MicroservicesIntegrationTest#testUserRegistration
```

## Test Cases

The `MicroservicesIntegrationTest` class contains the following test cases:

### 1. User Registration Tests (DataProvider)
- Valid user registration
- User with different email
- User with ADMIN role
- User with minimum required fields
- User with special characters in username

### 2. User Login Tests (DataProvider)
- Valid login
- Invalid password
- Non-existent user
- Empty username
- Empty password

### 3. Get User Details
- Retrieve user information with valid token

### 4. Order Creation Tests (DataProvider)
- Valid order with single item
- Order with multiple quantities
- Order with high value item
- Order with decimal price
- Order with zero quantity (should fail)

### 5. Get Order Details
- Retrieve order by order number

### 6. Get User Orders
- Retrieve all orders for a user

### 7. Get Notifications
- Retrieve notifications for a user

### 8. Get Payment Details
- Retrieve payment information for an order

### 9. End-to-End Flow Test
- Complete flow: Register → Login → Create Order → Get Order → Get Payment

### 10. Authentication Failure - Missing Token
- Test unauthorized access without token

### 11. Authentication Failure - Invalid Token
- Test unauthorized access with invalid token

## DataProvider Pattern

The test class uses TestNG DataProvider pattern to run multiple test scenarios:

```java
@DataProvider(name = "userRegistrationData")
public Object[][] getUserRegistrationData() {
    return new Object[][]{
        {"username", "email", "password", "role", expectedStatus},
        // ... more test cases
    };
}

@Test(dataProvider = "userRegistrationData")
public void testUserRegistration(...) {
    // Test implementation
}
```

## Configuration

### TestNG Configuration

Edit `src/test/resources/testng.xml` to configure:
- Test suite name
- Parallel execution settings
- Thread count
- Test classes

### API Base URL

The base URL is configured in the test class:
```java
private static final String BASE_URL = "http://localhost:8080";
```

To change the base URL, modify the `BASE_URL` constant in `MicroservicesIntegrationTest.java`.

## Test Reports

Test results are generated in:
- `target/surefire-reports/` - Surefire test reports
- Console output - Real-time test execution logs

## Troubleshooting

1. **Connection Refused**: Ensure microservices are running and API Gateway is accessible
2. **Test Failures**: Check that services are fully started before running tests
3. **Timeout Issues**: Increase timeout in RestAssured configuration if needed
4. **Port Conflicts**: Ensure port 8080 is not used by another application

## Adding New Test Cases

1. Add test data to appropriate DataProvider method
2. Create new test method with `@Test` annotation
3. Use helper methods (`registerAndGetToken`, `createOrderAndGetOrderNumber`) for common operations
4. Follow naming convention: `test<FeatureName>`

## Example Test Execution Output

```
[INFO] Running com.dissertation.integrationtestautomation.tests.MicroservicesIntegrationTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 45.234 s
[INFO] Results:
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

## CI/CD Integration

This project can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run Integration Tests
  run: |
    cd integrationtestautomation
    mvn clean test
```

## License

This project is part of the dissertation microservices project.

