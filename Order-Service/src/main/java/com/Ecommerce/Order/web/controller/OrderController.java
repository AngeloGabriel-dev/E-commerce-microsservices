package com.Ecommerce.Order.web.controller;

import com.Ecommerce.Order.dto.OrderCreateDto;
import com.Ecommerce.Order.dto.OrderResponseDto;
import com.Ecommerce.Order.service.OrderService;
import com.Ecommerce.Order.web.exceptions.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "security")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Resource to create a new order. Only users with CLIENT role can access.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Order created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid input data",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied. Only CLIENT role can create orders.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(Authentication authentication,
                                                        @RequestBody @Valid OrderCreateDto dto) {
        UUID clientId = UUID.fromString(authentication.getCredentials().toString());
        log.info("REST request to create order for client: {}", clientId);
        OrderResponseDto response = orderService.createOrder(dto, clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List orders by client", description = "Resource to list orders of the authenticated client with pagination",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
            })
    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderResponseDto>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to list orders for user: {}", authentication.getName());
        UUID clientId = UUID.fromString(authentication.getCredentials().toString());
        Page<OrderResponseDto> orders = orderService.findOrdersByClientId(clientId, page, size);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get order by ID", description = "Resource to get a specific order by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Order not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable UUID id) {
        log.info("REST request to get order by id: {}", id);
        OrderResponseDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
}