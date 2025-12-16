package com.example.JobTracker.security;

import com.example.JobTracker.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ✅ VERY IMPORTANT: Allow CORS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // ✅ Skip public endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            sendUnauthorizedError(response, "Missing or invalid authorization header");
            return;
        }

        String jwt = authorizationHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(jwt)) {
                sendUnauthorizedError(response, "Invalid or expired token");
                return;
            }

            // Extract claims
            String email = jwtUtil.extractEmail(jwt);
            Long userId = jwtUtil.extractUserId(jwt);
            String role = jwtUtil.extractRole(jwt);

            // Attach to request
            request.setAttribute("userId", userId);
            request.setAttribute("email", email);
            request.setAttribute("role", role);
            request.setAttribute("isAuthenticated", true);

            // Admin check
            if (path.startsWith("/api/admin") && !"ADMIN".equals(role)) {
                sendForbiddenError(response, "Admin access required");
                return;
            }

        } catch (Exception e) {
            sendUnauthorizedError(response, "Token validation failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/users/login")
                || path.equals("/api/users/register")
                || path.equals("/api/admin/login")
                || path.startsWith("/api/users/health")
                || path.startsWith("/api/jobs/health")
                || path.startsWith("/api/admin/health");
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("status", 401);
        error.put("timestamp", System.currentTimeMillis());

        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }

    private void sendForbiddenError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("status", 403);
        error.put("timestamp", System.currentTimeMillis());

        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
