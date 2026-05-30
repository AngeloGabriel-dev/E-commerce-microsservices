package com.Ecommerce.Auth.dto;

import com.Ecommerce.Auth.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserCreateDto {
    @NotBlank(message = "E-mail is required")
    @Email(message = "Invalid e-mail format")
    @Pattern(
            regexp = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$",
            message = "Invalid e-mail format"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50, message = "Password must have between 6 and 50 characters")
    /*@Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
            message = "Password must contain uppercase, lowercase and number"
    )*/
    private String password;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must have between 2 and 100 characters")
    @Pattern(
            regexp = "^[A-Za-zÀ-ÿ\\s]+$",
            message = "Name can only contain letters and spaces"
    )
    private String name;

    @NotNull(message = "Role is required")
    private User.Role role;

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

}
