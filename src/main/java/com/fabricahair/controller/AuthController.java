package com.fabricahair.controller;

import com.fabricahair.security.JwtTokenProvider;
import com.fabricahair.security.UserDetailsServiceImpl;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth") @CrossOrigin(origins = "*")
public class AuthController {
    @Autowired private AuthenticationManager authManager;
    @Autowired private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> req, HttpServletResponse res) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.get("username"), req.get("password")));
            String jwt = tokenProvider.generateToken(auth);
            UserDetails user = (UserDetails) auth.getPrincipal();
            Cookie cookie = new Cookie("token", jwt);
            cookie.setHttpOnly(true); cookie.setPath("/"); cookie.setMaxAge(86400);
            res.addCookie(cookie);
            return ResponseEntity.ok(Map.of(
                "accessToken", jwt, "tokenType", "Bearer",
                "username", user.getUsername(),
                "role", user.getAuthorities().iterator().next().getAuthority()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Usuário ou senha inválidos."));
        }
    }
}
