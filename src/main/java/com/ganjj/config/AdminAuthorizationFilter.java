package com.ganjj.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminAuthorizationFilter implements Filter {
    private static final List<String> PROTECTED_ADMIN_PATHS = Arrays.asList(
            "/api/users/admin",
            "/api/users/",
            "/api/products/admin",
            "/api/products/",
            "/api/categories/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        if (isAdminProtectedRoute(path, method)) {
            String adminHeader = httpRequest.getHeader("X-Admin-Role");
            
            if (adminHeader == null || !adminHeader.equals("ADMIN")) {
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
        
        for (String protectedPath : PROTECTED_ADMIN_PATHS) {
            if (path.startsWith(protectedPath)) {
                return true;
            }
        }
        
        return false;
    }
}