package com.ganjj.controller;

import com.ganjj.dto.*;
import com.ganjj.service.ShoppingBagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ShoppingBagResponseDTO> createShoppingBag(@Valid @RequestBody ShoppingBagCreateDTO createDTO) {
        ShoppingBagResponseDTO response = shoppingBagService.createShoppingBag(createDTO);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.getId()).toUri();
        
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingBagResponseDTO> getShoppingBag(@PathVariable Long id) {
        ShoppingBagResponseDTO response = shoppingBagService.getShoppingBag(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ShoppingBagSummaryDTO>> getUserShoppingBags(@PathVariable Long userId) {
        List<ShoppingBagSummaryDTO> bags = shoppingBagService.getUserShoppingBags(userId);
        return ResponseEntity.ok(bags);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ShoppingBagResponseDTO> getActiveShoppingBag(@PathVariable Long userId) {
        ShoppingBagResponseDTO response = shoppingBagService.getActiveShoppingBag(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ShoppingBagResponseDTO> addItemToShoppingBag(
            @PathVariable Long id,
            @Valid @RequestBody ShoppingBagItemDTO itemDTO) {
        ShoppingBagResponseDTO response = shoppingBagService.addItemToShoppingBag(id, itemDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bagId}/items/{itemId}")
    public ResponseEntity<ShoppingBagResponseDTO> updateItemQuantity(
            @PathVariable Long bagId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        ShoppingBagResponseDTO response = shoppingBagService.updateItemQuantity(bagId, itemId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bagId}/items/{itemId}")
    public ResponseEntity<ShoppingBagResponseDTO> removeItem(
            @PathVariable Long bagId,
            @PathVariable Long itemId) {
        ShoppingBagResponseDTO response = shoppingBagService.removeItem(bagId, itemId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ShoppingBagResponseDTO> updateShoppingBagStatus(
            @PathVariable Long id,
            @Valid @RequestBody ShoppingBagStatusDTO statusDTO) {
        ShoppingBagResponseDTO response = shoppingBagService.updateShoppingBagStatus(id, statusDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShoppingBag(@PathVariable Long id) {
        shoppingBagService.deleteShoppingBag(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/clear")
    public ResponseEntity<ShoppingBagResponseDTO> clearShoppingBag(@PathVariable Long id) {
        ShoppingBagResponseDTO response = shoppingBagService.clearShoppingBag(id);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}