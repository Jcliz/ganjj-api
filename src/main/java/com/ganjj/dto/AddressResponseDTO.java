package com.ganjj.dto;

import com.ganjj.entities.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AddressResponseDTO {

    private Long id;
    private Long userId;
    private String recipientName;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private Address.AddressType type;
    private Boolean isDefault;
    private Boolean active;
    private String reference;
    private String fullAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AddressResponseDTO(Address address) {
        this.id = address.getId();
        this.userId = address.getUser().getId();
        this.recipientName = address.getRecipientName();
        this.street = address.getStreet();
        this.number = address.getNumber();
        this.complement = address.getComplement();
        this.neighborhood = address.getNeighborhood();
        this.city = address.getCity();
        this.state = address.getState();
        this.zipCode = address.getZipCode();
        this.phone = address.getPhone();
        this.type = address.getType();
        this.isDefault = address.getIsDefault();
        this.active = address.getActive();
        this.reference = address.getReference();
        this.fullAddress = address.getFullAddress();
        this.createdAt = address.getCreatedAt();
        this.updatedAt = address.getUpdatedAt();
    }
}
