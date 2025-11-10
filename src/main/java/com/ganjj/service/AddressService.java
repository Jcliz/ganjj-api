package com.ganjj.service;

import com.ganjj.dto.AddressCreateDTO;
import com.ganjj.dto.AddressResponseDTO;
import com.ganjj.dto.AddressUpdateDTO;
import com.ganjj.entities.Address;
import com.ganjj.entities.User;
import com.ganjj.repository.AddressRepository;
import com.ganjj.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AddressResponseDTO createAddress(AddressCreateDTO createDTO) {
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + createDTO.getUserId()));

        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(createDTO.getRecipientName());
        address.setStreet(createDTO.getStreet());
        address.setNumber(createDTO.getNumber());
        address.setComplement(createDTO.getComplement());
        address.setNeighborhood(createDTO.getNeighborhood());
        address.setCity(createDTO.getCity());
        address.setState(createDTO.getState().toUpperCase());
        address.setZipCode(createDTO.getZipCode());
        address.setPhone(createDTO.getPhone());
        address.setType(createDTO.getType() != null ? createDTO.getType() : Address.AddressType.HOME);
        address.setReference(createDTO.getReference());

        List<Address> userAddresses = addressRepository.findByUserIdAndActiveTrue(user.getId());
        boolean isFirstAddress = userAddresses.isEmpty();
        boolean shouldBeDefault = (createDTO.getIsDefault() != null && createDTO.getIsDefault()) || isFirstAddress;

        if (shouldBeDefault) {
            userAddresses.forEach(addr -> {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            });
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        Address savedAddress = addressRepository.save(address);
        return new AddressResponseDTO(savedAddress);
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Usuário não encontrado com o ID: " + userId);
        }
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .map(AddressResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponseDTO getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com o ID: " + id));
        return new AddressResponseDTO(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAllAddresses() {
        return addressRepository.findAll().stream()
                .map(AddressResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void validateAddressOwnership(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com o ID: " + addressId));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new SecurityException("Você não tem permissão para acessar este endereço.");
        }
    }

    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressUpdateDTO updateDTO) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com o ID: " + id));

        if (!address.getActive()) {
            throw new IllegalStateException("Não é possível editar um endereço inativo.");
        }

        if (updateDTO.getRecipientName() != null) {
            address.setRecipientName(updateDTO.getRecipientName());
        }

        if (updateDTO.getStreet() != null) {
            address.setStreet(updateDTO.getStreet());
        }

        if (updateDTO.getNumber() != null) {
            address.setNumber(updateDTO.getNumber());
        }

        if (updateDTO.getComplement() != null) {
            address.setComplement(updateDTO.getComplement());
        }

        if (updateDTO.getNeighborhood() != null) {
            address.setNeighborhood(updateDTO.getNeighborhood());
        }

        if (updateDTO.getCity() != null) {
            address.setCity(updateDTO.getCity());
        }

        if (updateDTO.getState() != null) {
            address.setState(updateDTO.getState().toUpperCase());
        }

        if (updateDTO.getZipCode() != null) {
            address.setZipCode(updateDTO.getZipCode());
        }

        if (updateDTO.getPhone() != null) {
            address.setPhone(updateDTO.getPhone());
        }

        if (updateDTO.getType() != null) {
            address.setType(updateDTO.getType());
        }

        if (updateDTO.getReference() != null) {
            address.setReference(updateDTO.getReference());
        }

        if (updateDTO.getActive() != null) {
            address.setActive(updateDTO.getActive());
        }

        if (updateDTO.getIsDefault() != null && updateDTO.getIsDefault()) {
            setAsDefault(id);
        }

        Address updatedAddress = addressRepository.save(address);
        return new AddressResponseDTO(updatedAddress);
    }

    @Transactional
    public AddressResponseDTO setAsDefault(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com o ID: " + id));

        List<Address> userAddresses = addressRepository.findByUserId(address.getUser().getId());
        userAddresses.forEach(addr -> {
            addr.setIsDefault(false);
            addressRepository.save(addr);
        });

        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);
        return new AddressResponseDTO(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com o ID: " + id));

        List<Address> userAddresses = addressRepository.findByUserIdAndActiveTrue(address.getUser().getId());
        
        if (userAddresses.size() == 1 && userAddresses.get(0).getId().equals(id)) {
            throw new IllegalStateException("Não é possível excluir o único endereço ativo do usuário.");
        }

        if (address.getIsDefault()) {
            List<Address> otherAddresses = addressRepository.findByUserIdAndActiveTrue(address.getUser().getId())
                    .stream()
                    .filter(addr -> !addr.getId().equals(id))
                    .collect(Collectors.toList());

            if (!otherAddresses.isEmpty()) {
                Address newDefault = otherAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }

        addressRepository.delete(address);
    }
}
