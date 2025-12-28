package com.dissertation.integrationtestautomation.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

/**
 * Custom TestNG listener to provide detailed error reporting
 * Helps display assertion errors and test failures in IDE output
 */
public class TestNGListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("\n==========================================");
        System.out.println("TEST FAILED: " + result.getMethod().getMethodName());
        System.out.println("==========================================");
        
        // Print test parameters if available
        if (result.getParameters() != null && result.getParameters().length > 0) {
            System.out.print("Parameters: ");
            for (int i = 0; i < result.getParameters().length; i++) {
                System.out.print(result.getParameters()[i]);
                if (i < result.getParameters().length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        
        // Print the exception/error
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            System.out.println("\nError Message: " + throwable.getMessage());
            System.out.println("\nStack Trace:");
            throwable.printStackTrace(System.out);
        }
        
        // Print reporter messages (if any)
        if (Reporter.getOutput(result).size() > 0) {
            System.out.println("\nReporter Messages:");
            for (String message : Reporter.getOutput(result)) {
                System.out.println("  - " + message);
            }
        }
        
        System.out.println("==========================================\n");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("✓ PASSED: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("⊘ SKIPPED: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            System.out.println("  Reason: " + result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("▶ RUNNING: " + result.getMethod().getMethodName());
    }
}

