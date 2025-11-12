package com.ganjj.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminAuthorizationFilter implements Filter {
    private static final List<String> PROTECTED_ADMIN_PATHS = Arrays.asList(
            "/api/users/admin",
            "/api/users/",
            "/api/products/admin",
            "/api/products/",
            "/api/categories/",
            "/api/brands/admin"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        if (isAdminProtectedRoute(path, method)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("Não autenticado");
                return;
            }
            
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("Acesso negado: Privilégios de administrador necessários");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isAdminProtectedRoute(String path, String method) {
        if (path.equals("/api/users") && method.equals("POST")) {
            return false;
        }

        if (path.equals("/api/products") && method.equals("GET")) {
            return false;
        }
        
        if (path.startsWith("/api/products/") && method.equals("GET") && 
            !path.startsWith("/api/products/admin/")) {
            return false; 
        }
        
        if (path.equals("/api/categories") && method.equals("GET")) {
            return false; 
        }
        
        if (path.startsWith("/api/categories/") && method.equals("GET") && 
            !path.startsWith("/api/categories/admin/")) {
            return false; 
        }
        
        if (path.equals("/api/brands") && method.equals("GET")) {
            return false; 
        }
        
        for (String protectedPath : PROTECTED_ADMIN_PATHS) {
            if (path.startsWith(protectedPath)) {
                return true;
            }
        }
        
        return false;
    }
}