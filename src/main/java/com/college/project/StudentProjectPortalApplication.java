package com.college.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application Entry Point for the College Student Project
 * Management & Evaluation Portal (SPMEP).
 *
 * Starts the embedded Tomcat server on port 8080 and bootstraps Spring context.
 */
@SpringBootApplication
public class StudentProjectPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentProjectPortalApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("  Student Project Management Portal Backend Started Successfully  ");
        System.out.println("  API Base URL: http://localhost:8080/api                         ");
        System.out.println("  H2 Console:   http://localhost:8080/h2-console                  ");
        System.out.println("==================================================================");
    }
}
