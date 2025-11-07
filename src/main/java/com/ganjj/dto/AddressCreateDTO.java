package com.ganjj.dto;

import com.ganjj.entities.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressCreateDTO {

    @NotNull(message = "O ID do usuário é obrigatório.")
    private Long userId;

    @NotBlank(message = "O nome do destinatário é obrigatório.")
    private String recipientName;

    @NotBlank(message = "A rua é obrigatória.")
    private String street;

    @NotBlank(message = "O número é obrigatório.")
    private String number;

    private String complement;

    @NotBlank(message = "O bairro é obrigatório.")
    private String neighborhood;

    @NotBlank(message = "A cidade é obrigatória.")
    private String city;

    @NotBlank(message = "O estado é obrigatório.")
    @Size(min = 2, max = 2, message = "O estado deve ter 2 caracteres (UF).")
    @Pattern(regexp = "[A-Z]{2}", message = "O estado deve estar em maiúsculas (ex: SP, RJ).")
    private String state;

    @NotBlank(message = "O CEP é obrigatório.")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato 12345-678.")
    private String zipCode;

    @NotBlank(message = "O telefone é obrigatório.")
    private String phone;

    private Address.AddressType type;

    private Boolean isDefault;

    private String reference;
}
