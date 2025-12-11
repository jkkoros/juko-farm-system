package com.example.farmmanagement.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
public class DesktopLoginController {

    @GetMapping("/desktop-login")
    public String desktopLogin(@RequestParam String user) {
        var auth = new UsernamePasswordAuthenticationToken(
            user, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        return "redirect:/dashboard";  // or /farms
    }
}
