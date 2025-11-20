/*
 * @ (#) HealthController.java    1.0    30/09/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.controllers;/*
 * @description:
 * @author: Bao Thong
 * @date: 30/09/2025
 * @version: 1.0
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@RequestMapping("/health")
public class HealthController {
    @Autowired
    private DataSource dataSource;

    @GetMapping("/db")
    public ResponseEntity<String> checkDbConnection() {
        try (Connection conn = dataSource.getConnection()) {
            return ResponseEntity.ok("Database connected!");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Database connection failed: " + e.getMessage());
        }
    }
}
