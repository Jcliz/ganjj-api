package com.ganjj.controller;

import com.ganjj.dto.*;
import com.ganjj.security.UserDetailsImpl;
import com.ganjj.service.ShoppingBagService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/shopping-bags")
public class ShoppingBagController {

    @Autowired
    private ShoppingBagService shoppingBagService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ShoppingBagResponseDTO> createShoppingBag(
            @Valid @RequestBody ShoppingBagCreateDTO createDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !createDTO.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ShoppingBagResponseDTO response = shoppingBagService.createShoppingBag(createDTO);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ShoppingBagResponseDTO> getShoppingBag(@PathVariable Long id, Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        ShoppingBagResponseDTO response = shoppingBagService.getShoppingBag(id);
        
        if (!isAdmin && !response.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ShoppingBagSummaryDTO>> getUserShoppingBags(@PathVariable Long userId, Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !userId.equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<ShoppingBagSummaryDTO> bags = shoppingBagService.getUserShoppingBags(userId);
        return ResponseEntity.ok(bags);
    }

    @GetMapping("/user/{userId}/active")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ShoppingBagResponseDTO> getActiveShoppingBag(@PathVariable Long userId, Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !userId.equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ShoppingBagResponseDTO response = shoppingBagService.getActiveShoppingBag(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ShoppingBagResponseDTO> addItemToShoppingBag(
            @PathVariable Long id,
            @Valid @RequestBody ShoppingBagItemDTO itemDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            ShoppingBagResponseDTO shoppingBag = shoppingBagService.getShoppingBag(id);
            if (!shoppingBag.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        ShoppingBagResponseDTO response = shoppingBagService.addItemToShoppingBag(id, itemDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bagId}/items/{itemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ShoppingBagResponseDTO> updateItemQuantity(
            @PathVariable Long bagId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            ShoppingBagResponseDTO shoppingBag = shoppingBagService.getShoppingBag(bagId);
            if (!shoppingBag.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        ShoppingBagResponseDTO response = shoppingBagService.updateItemQuantity(bagId, itemId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bagId}/items/{itemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ShoppingBagResponseDTO> removeItem(
            @PathVariable Long bagId,
            @PathVariable Long itemId,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            ShoppingBagResponseDTO shoppingBag = shoppingBagService.getShoppingBag(bagId);
            if (!shoppingBag.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        ShoppingBagResponseDTO response = shoppingBagService.removeItem(bagId, itemId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ShoppingBagResponseDTO> updateShoppingBagStatus(
            @PathVariable Long id,
            @Valid @RequestBody ShoppingBagStatusDTO statusDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            ShoppingBagResponseDTO shoppingBag = shoppingBagService.getShoppingBag(id);
            if (!shoppingBag.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        ShoppingBagResponseDTO response = shoppingBagService.updateShoppingBagStatus(id, statusDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteShoppingBag(@PathVariable Long id, Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            ShoppingBagResponseDTO shoppingBag = shoppingBagService.getShoppingBag(id);
            if (!shoppingBag.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        shoppingBagService.deleteShoppingBag(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/clear")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ShoppingBagResponseDTO> clearShoppingBag(@PathVariable Long id, Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            ShoppingBagResponseDTO shoppingBag = shoppingBagService.getShoppingBag(id);
            if (!shoppingBag.getUserId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        ShoppingBagResponseDTO response = shoppingBagService.clearShoppingBag(id);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}