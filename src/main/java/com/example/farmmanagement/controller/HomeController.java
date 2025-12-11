package com.example.farmmanagement.controller;

import com.example.farmmanagement.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ActivityLogService logService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("recentLogs", logService.getRecentLogs());
        return "dashboard";  // loads dashboard.html
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
