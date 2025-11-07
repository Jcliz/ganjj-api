package com.ganjj.dto;

import com.ganjj.entities.Address;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressUpdateDTO {

    private String recipientName;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;

    @Size(min = 2, max = 2, message = "O estado deve ter 2 caracteres (UF).")
    @Pattern(regexp = "[A-Z]{2}", message = "O estado deve estar em maiúsculas (ex: SP, RJ).")
    private String state;

    @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato 12345-678.")
    private String zipCode;

    private String phone;
    private Address.AddressType type;
    private Boolean isDefault;
    private Boolean active;
    private String reference;
}
