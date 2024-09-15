package com.chillteq.channel_archive_server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/health")
public class HealthCheckController {

    Logger logger = Logger.getLogger(HealthCheckController.class.getName());

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check API hit");
        return ResponseEntity.ok("UP");
    }
}
