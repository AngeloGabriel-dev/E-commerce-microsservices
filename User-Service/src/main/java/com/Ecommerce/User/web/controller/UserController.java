package com.Ecommerce.User.web.controller;

import com.Ecommerce.User.dto.SellerResponseDto;
import com.Ecommerce.User.service.UserService;
import com.Ecommerce.User.web.exceptions.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

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
}