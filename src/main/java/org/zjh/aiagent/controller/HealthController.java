package org.zjh.aiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kayson
 * @date 2026/5/20
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/get")
    public String getHealth() {
        return "ok";
    }
}
