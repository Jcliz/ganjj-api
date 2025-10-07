package com.ganjj.security;

import com.ganjj.dto.JwtResponseDTO;
import com.ganjj.dto.LoginRequestDTO;
import com.ganjj.dto.TokenRefreshRequestDTO;
import com.ganjj.dto.TokenRefreshResponseDTO;
import com.ganjj.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshTokenFromUsername(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDTO(
                jwt,
                refreshToken,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail(),
                roles.get(0).replace("ROLE_", ""),
                roles));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request) {
        String requestRefreshToken = request.getRefreshToken();

        if (!jwtUtils.validateJwtToken(requestRefreshToken)) {
            return ResponseEntity.badRequest().body("Error: Invalid refresh token!");
        }
        
        String username = jwtUtils.getUsernameFromJwtToken(requestRefreshToken);
        String newToken = jwtUtils.generateTokenFromUsername(username);
        
        return ResponseEntity.ok(new TokenRefreshResponseDTO(newToken, requestRefreshToken, "Bearer"));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout realizado com sucesso!");
    }
}