package com.ganjj.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_addresses")
@Data
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String number;

    private String complement;

    @Column(nullable = false)
    private String neighborhood;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 2)
    private String state; 

    @Column(nullable = false, length = 9)
    private String zipCode;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    private AddressType type = AddressType.HOME;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Boolean active = true;

    private String reference;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AddressType {
        HOME,
        WORK,
        OTHER
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(street).append(", ").append(number);
        if (complement != null && !complement.isEmpty()) {
            sb.append(" - ").append(complement);
        }
        sb.append(", ").append(neighborhood);
        sb.append(", ").append(city).append(" - ").append(state);
        sb.append(", CEP: ").append(zipCode);
        return sb.toString();
    }
}
