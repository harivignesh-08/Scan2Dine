package com.scan2dine.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping("/superadmin")
    public String forwardSpaRoutes() {
        // Forward client-side path routes to index.html for React SPA routing
        return "forward:/index.html";
    }
}
