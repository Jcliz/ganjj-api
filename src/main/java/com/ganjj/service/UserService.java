package com.ganjj.service;

import com.ganjj.dto.UserCreateDTO;
import com.ganjj.dto.UserResponseDTO;
import com.ganjj.entities.User;
import com.ganjj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        if (userRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        User newUser = new User();
        newUser.setName(userCreateDTO.getName());
        newUser.setEmail(userCreateDTO.getEmail());

        newUser.setPassword(userCreateDTO.getPassword());

        newUser.setAddress(userCreateDTO.getAddress());

        User savedUser = userRepository.save(newUser);

        return new UserResponseDTO(savedUser);
    }
}