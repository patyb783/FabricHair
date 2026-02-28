package com.fabricahair.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException, ServletException {
        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains("text/html")) {
            res.sendRedirect("/login?error=true");
        } else {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autorizado.");
        }
    }
}
