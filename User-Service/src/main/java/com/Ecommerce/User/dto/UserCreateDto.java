package com.Ecommerce.User.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserCreateDto {
    @NotNull(message = "Id is required")
    private UUID id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must have between 2 and 100 characters")
    @Pattern(
            regexp = "^[A-Za-zÀ-ÿ\\s]+$",
            message = "Name can only contain letters and spaces"
    )
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;

    @NotBlank(message = "CPF is required")
    @Pattern(
            regexp = "^\\d{11}$",
            message = "CPF must contain exactly 11 digits"
    )
    private String cpf;

    @NotBlank(message = "E-mail is required")
    @Email(message = "Invalid e-mail format")
    @Pattern(
            regexp = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$",
            message = "Invalid e-mail format"
    )
    private String email;
}