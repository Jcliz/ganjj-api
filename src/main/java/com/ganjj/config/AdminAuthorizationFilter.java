package com.ganjj.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AdminAuthorizationFilter implements Filter {

    private static final List<String> PROTECTED_ADMIN_PATHS = Arrays.asList(
            "/api/users/admin",
            "/api/users/"
    );

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

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
        
        for (String protectedPath : PROTECTED_ADMIN_PATHS) {
            if (path.startsWith(protectedPath)) {
                return true;
            }
        }
        
        return false;
    }
}