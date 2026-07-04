package com.Ecommerce.User.web.controller;

import com.Ecommerce.User.dto.SellerResponseDto;
import com.Ecommerce.User.dto.UserContactInfoDto;
import com.Ecommerce.User.dto.UserCreateDto;
import com.Ecommerce.User.entity.User;
import com.Ecommerce.User.service.UserService;
import com.Ecommerce.User.web.exceptions.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create user", description = "Resource to create a new user in the user-service database.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid input data",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid UserCreateDto dto) {
        log.info("REST request to create user: {}", dto.getEmail());
        User savedUser = userService.saveUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Delete user by ID", description = "Resource to delete a user by ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("REST request to delete user by id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get seller by ID", description = "Resource to get a seller's public information by ID. No authentication required.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Seller found successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SellerResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Seller not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/sellers/{id}")
    public ResponseEntity<SellerResponseDto> getSellerById(@PathVariable UUID id) {
        log.info("REST request to get seller by id: {}", id);
        SellerResponseDto seller = userService.getSellerById(id);
        return ResponseEntity.ok(seller);
    }

    @Operation(summary = "Get user contact info by ID", description = "Resource to get user email and phone number by ID. Only accessible by Notification Service.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User contact info found successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserContactInfoDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied - Only Notification Service can access this endpoint",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}/contact-info")
    @PreAuthorize("hasRole('ROLE_NOTIFICATION_SERVICE')")
    public ResponseEntity<UserContactInfoDto> getUserContactInfo(@PathVariable UUID id) {
        log.info("REST request to get user contact info by id: {}", id);
        UserContactInfoDto contactInfo = userService.getUserContactInfo(id);
        return ResponseEntity.ok(contactInfo);
    }
}
