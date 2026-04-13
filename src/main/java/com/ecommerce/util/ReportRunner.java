package com.ecommerce.util;

/**
 * Utility to run validation and performance reports.
 */
public class ReportRunner {
    public static void main(String[] args) {
        System.out.println("Starting Application Verification...");
        try {
            ReportGenerator gen = new ReportGenerator();
            
            System.out.println("Running Validation Suite...");
            gen.generateValidationReport("validation_report.txt");
            
            System.out.println("Running Performance Suite...");
            gen.generatePerformanceReport("performance_report.txt");
            
            System.out.println("Verification Complete. Check validation_report.txt and performance_report.txt");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
