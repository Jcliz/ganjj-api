package com.ganjj.service;

import com.ganjj.dto.*;
import com.ganjj.entities.ShoppingBag;
import com.ganjj.entities.ShoppingBagItem;
import com.ganjj.entities.User;
import com.ganjj.exception.ConflictException;
import com.ganjj.exception.ErrorCode;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.ShoppingBagItemRepository;
import com.ganjj.repository.ShoppingBagRepository;
import com.ganjj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShoppingBagService {

    @Autowired
    private ShoppingBagRepository shoppingBagRepository;

    @Autowired
    private ShoppingBagItemRepository shoppingBagItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ShoppingBagResponseDTO createShoppingBag(ShoppingBagCreateDTO createDTO) {
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Optional<ShoppingBag> existingOpenBag = shoppingBagRepository
                .findByUserAndStatusOrderByCreatedAtDesc(user, ShoppingBag.ShoppingBagStatus.OPEN);
        
        if (existingOpenBag.isPresent()) {
            return new ShoppingBagResponseDTO(existingOpenBag.get());
        }
        
        ShoppingBag shoppingBag = new ShoppingBag();
        shoppingBag.setUser(user);
        shoppingBag.setStatus(ShoppingBag.ShoppingBagStatus.OPEN);
        
        ShoppingBag savedShoppingBag = shoppingBagRepository.save(shoppingBag);
        
        return new ShoppingBagResponseDTO(savedShoppingBag);
    }

    @Transactional(readOnly = true)
    public ShoppingBagResponseDTO getShoppingBag(Long id) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND));
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }

    @Transactional(readOnly = true)
    public List<ShoppingBagSummaryDTO> getUserShoppingBags(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        
        List<ShoppingBag> bags = shoppingBagRepository.findByUser(user);
        return ShoppingBagSummaryDTO.fromEntities(bags);
    }

    @Transactional(readOnly = true)
    public ShoppingBagResponseDTO getActiveShoppingBag(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        
        ShoppingBag shoppingBag = shoppingBagRepository
                .findByUserAndStatusOrderByCreatedAtDesc(user, ShoppingBag.ShoppingBagStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND));
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }

    @Transactional
    public ShoppingBagResponseDTO addItemToShoppingBag(Long bagId, ShoppingBagItemDTO itemDTO) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(bagId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND));
        
        if (shoppingBag.getStatus() != ShoppingBag.ShoppingBagStatus.OPEN) {
            throw new ConflictException(ErrorCode.SHOPPING_BAG_NOT_OPEN);
        }
        
        Optional<ShoppingBagItem> existingItem = shoppingBagItemRepository
                .findByShoppingBagAndProductIdAndSize(shoppingBag, itemDTO.getProductId(), itemDTO.getSize());
        
        ShoppingBagItem bagItem;
        
        if (existingItem.isPresent()) {
            bagItem = existingItem.get();
            bagItem.setQuantity(bagItem.getQuantity() + itemDTO.getQuantity());
        } else {
            bagItem = new ShoppingBagItem();
            bagItem.setProductId(itemDTO.getProductId());
            bagItem.setProductName(itemDTO.getProductName());
            bagItem.setSize(itemDTO.getSize());
            bagItem.setPrice(itemDTO.getPrice());
            bagItem.setQuantity(itemDTO.getQuantity());
            
            shoppingBag.addItem(bagItem);
        }
        
        shoppingBagItemRepository.save(bagItem);
        shoppingBag.recalculateTotalAmount();
        shoppingBagRepository.save(shoppingBag);
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }

    @Transactional
    public ShoppingBagResponseDTO updateItemQuantity(Long bagId, Long itemId, Integer quantity) {
        if (quantity <= 0) {
            throw new ValidationException(ErrorCode.SHOPPING_BAG_INVALID_QUANTITY);
        }
        
        ShoppingBag shoppingBag = shoppingBagRepository.findById(bagId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND));
        
        if (shoppingBag.getStatus() != ShoppingBag.ShoppingBagStatus.OPEN) {
            throw new ConflictException(ErrorCode.SHOPPING_BAG_NOT_OPEN);
        }
        
        ShoppingBagItem item = shoppingBagItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_ITEM_NOT_FOUND));
        
        if (!item.getShoppingBag().getId().equals(bagId)) {
            throw new ValidationException(ErrorCode.SHOPPING_BAG_ITEM_NOT_BELONGS_TO_BAG);
        }
        
        item.setQuantity(quantity);
        shoppingBagItemRepository.save(item);
        
        shoppingBag.recalculateTotalAmount();
        shoppingBagRepository.save(shoppingBag);
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }

    @Transactional
    public ShoppingBagResponseDTO removeItem(Long bagId, Long itemId) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(bagId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND));
        
        if (shoppingBag.getStatus() != ShoppingBag.ShoppingBagStatus.OPEN) {
            throw new ConflictException(ErrorCode.SHOPPING_BAG_CANNOT_REMOVE_FROM_CLOSED);
        }
        
        ShoppingBagItem item = shoppingBagItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_ITEM_NOT_FOUND));
        
        if (!item.getShoppingBag().getId().equals(bagId)) {
            throw new ValidationException(ErrorCode.SHOPPING_BAG_ITEM_NOT_BELONGS_TO_BAG);
        }
        
        shoppingBag.removeItem(item);
        shoppingBagItemRepository.delete(item);
        
        shoppingBag.recalculateTotalAmount();
        shoppingBagRepository.save(shoppingBag);
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }

    @Transactional
    public ShoppingBagResponseDTO updateShoppingBagStatus(Long bagId, ShoppingBagStatusDTO statusDTO) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(bagId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND));
        
        try {
            ShoppingBag.ShoppingBagStatus newStatus = ShoppingBag.ShoppingBagStatus.valueOf(statusDTO.getStatus());
            shoppingBag.setStatus(newStatus);
            shoppingBagRepository.save(shoppingBag);
            
            return new ShoppingBagResponseDTO(shoppingBag);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ErrorCode.SHOPPING_BAG_INVALID_STATUS);
        }
    }

    @Transactional
    public void deleteShoppingBag(Long id) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND));
        
        shoppingBagRepository.delete(shoppingBag);
    }

    @Transactional
    public ShoppingBagResponseDTO clearShoppingBag(Long id) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND));
        
        if (shoppingBag.getStatus() != ShoppingBag.ShoppingBagStatus.OPEN) {
            throw new ConflictException(ErrorCode.SHOPPING_BAG_CANNOT_CLEAR_CLOSED);
        }
        
        shoppingBag.getItems().clear();
        shoppingBag.recalculateTotalAmount();
        shoppingBagRepository.save(shoppingBag);
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }
}