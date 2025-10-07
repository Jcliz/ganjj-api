package com.ganjj.service;

import com.ganjj.dto.*;
import com.ganjj.entities.ShoppingBag;
import com.ganjj.entities.ShoppingBagItem;
import com.ganjj.entities.User;
import com.ganjj.repository.ShoppingBagItemRepository;
import com.ganjj.repository.ShoppingBagRepository;
import com.ganjj.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + createDTO.getUserId()));

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
                .orElseThrow(() -> new EntityNotFoundException("Sacola não encontrada com o ID: " + id));
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }

    @Transactional(readOnly = true)
    public List<ShoppingBagSummaryDTO> getUserShoppingBags(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + userId));
        
        List<ShoppingBag> bags = shoppingBagRepository.findByUser(user);
        return ShoppingBagSummaryDTO.fromEntities(bags);
    }

    @Transactional(readOnly = true)
    public ShoppingBagResponseDTO getActiveShoppingBag(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + userId));
        
        ShoppingBag shoppingBag = shoppingBagRepository
                .findByUserAndStatusOrderByCreatedAtDesc(user, ShoppingBag.ShoppingBagStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não possui uma sacola ativa"));
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }

    @Transactional
    public ShoppingBagResponseDTO addItemToShoppingBag(Long bagId, ShoppingBagItemDTO itemDTO) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(bagId)
                .orElseThrow(() -> new EntityNotFoundException("Sacola não encontrada com o ID: " + bagId));
        
        if (shoppingBag.getStatus() != ShoppingBag.ShoppingBagStatus.OPEN) {
            throw new IllegalStateException("Não é possível adicionar itens a uma sacola que não está aberta");
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
            bagItem.setProductImage(itemDTO.getProductImage());
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
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }
        
        ShoppingBag shoppingBag = shoppingBagRepository.findById(bagId)
                .orElseThrow(() -> new EntityNotFoundException("Sacola não encontrada com o ID: " + bagId));
        
        if (shoppingBag.getStatus() != ShoppingBag.ShoppingBagStatus.OPEN) {
            throw new IllegalStateException("Não é possível alterar itens de uma sacola que não está aberta");
        }
        
        ShoppingBagItem item = shoppingBagItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado com o ID: " + itemId));
        
        if (!item.getShoppingBag().getId().equals(bagId)) {
            throw new IllegalArgumentException("O item não pertence à sacola especificada");
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
                .orElseThrow(() -> new EntityNotFoundException("Sacola não encontrada com o ID: " + bagId));
        
        if (shoppingBag.getStatus() != ShoppingBag.ShoppingBagStatus.OPEN) {
            throw new IllegalStateException("Não é possível remover itens de uma sacola que não está aberta");
        }
        
        ShoppingBagItem item = shoppingBagItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado com o ID: " + itemId));
        
        if (!item.getShoppingBag().getId().equals(bagId)) {
            throw new IllegalArgumentException("O item não pertence à sacola especificada");
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
                .orElseThrow(() -> new EntityNotFoundException("Sacola não encontrada com o ID: " + bagId));
        
        try {
            ShoppingBag.ShoppingBagStatus newStatus = ShoppingBag.ShoppingBagStatus.valueOf(statusDTO.getStatus());
            shoppingBag.setStatus(newStatus);
            shoppingBagRepository.save(shoppingBag);
            
            return new ShoppingBagResponseDTO(shoppingBag);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + statusDTO.getStatus());
        }
    }

    @Transactional
    public void deleteShoppingBag(Long id) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sacola não encontrada com o ID: " + id));
        
        shoppingBagRepository.delete(shoppingBag);
    }

    @Transactional
    public ShoppingBagResponseDTO clearShoppingBag(Long id) {
        ShoppingBag shoppingBag = shoppingBagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sacola não encontrada com o ID: " + id));
        
        if (shoppingBag.getStatus() != ShoppingBag.ShoppingBagStatus.OPEN) {
            throw new IllegalStateException("Não é possível limpar uma sacola que não está aberta");
        }
        
        shoppingBag.getItems().clear();
        shoppingBag.recalculateTotalAmount();
        shoppingBagRepository.save(shoppingBag);
        
        return new ShoppingBagResponseDTO(shoppingBag);
    }
}