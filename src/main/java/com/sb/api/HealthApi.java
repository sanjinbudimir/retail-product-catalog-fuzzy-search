package com.sb.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthApi {

    @GetMapping("/health")
    public String health() {
        return "Server is running!";
    }
}
