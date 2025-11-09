package com.ganjj.service;

import com.ganjj.dto.UserCreateDTO;
import com.ganjj.dto.UserResponseDTO;
import com.ganjj.dto.UserUpdateDTO;
import com.ganjj.entities.User;
import com.ganjj.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        User newUser = new User();
        newUser.setName(userCreateDTO.getName());
        newUser.setEmail(userCreateDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        newUser.setPhone(userCreateDTO.getPhone());
        newUser.setRole(User.UserRole.USER);

        User savedUser = userRepository.save(newUser);

        return new UserResponseDTO(savedUser);
    }
    
    @Transactional
    public UserResponseDTO createAdminUser(UserCreateDTO userCreateDTO) {
        if (userRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        User newUser = new User();
        newUser.setName(userCreateDTO.getName());
        newUser.setEmail(userCreateDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        newUser.setPhone(userCreateDTO.getPhone());
        newUser.setRole(User.UserRole.ADMIN);

        User savedUser = userRepository.save(newUser);

        return new UserResponseDTO(savedUser);
    }
    
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        // Força a leitura do banco de dados, não do cache
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
                
        return new UserResponseDTO(user);
    }
    
    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
        
        // Agora o email pode ser alterado pois o JWT usa ID
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userUpdateDTO.getEmail()).isPresent()) {
                throw new IllegalArgumentException("E-mail já cadastrado para outro usuário.");
            }
            user.setEmail(userUpdateDTO.getEmail());
        }
        
        if (userUpdateDTO.getName() != null && !userUpdateDTO.getName().isEmpty()) {
            user.setName(userUpdateDTO.getName());
        }
        
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }
        
        if (userUpdateDTO.getPhone() != null) {
            user.setPhone(userUpdateDTO.getPhone());
        }
        
        User savedUser = userRepository.saveAndFlush(user);
        
        return new UserResponseDTO(savedUser);
    }
    
    @Transactional
    public UserResponseDTO updateUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
        
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            user.setRole(userRole);
            User savedUser = userRepository.save(user);
            
            return new UserResponseDTO(savedUser);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role inválida: " + role);
        }
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
        
        // O cascade ALL vai deletar automaticamente orders, addresses, reviews e shopping bags
        userRepository.delete(user);
        userRepository.flush();
    }
}