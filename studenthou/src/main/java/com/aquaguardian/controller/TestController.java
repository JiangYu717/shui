package com.aquaguardian.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from backend!";
    }

    @GetMapping("/checkReservoirName")
    public boolean checkReservoirName(@RequestParam String reservoirName) {
        return false; // 简单返回 false 用于测试
    }
} 