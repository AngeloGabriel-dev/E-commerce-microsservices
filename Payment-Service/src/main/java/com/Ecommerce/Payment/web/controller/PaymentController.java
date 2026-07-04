package com.Ecommerce.Payment.web.controller;

import com.Ecommerce.Payment.dto.PaymentCreateDto;
import com.Ecommerce.Payment.dto.PaymentResponseDto;
import com.Ecommerce.Payment.service.PaymentService;
import com.Ecommerce.Payment.web.exceptions.ErrorMessage;
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
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Process a new payment", description = "Resource to process a new payment for an order. Only users with CLIENT role can access.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Payment processed successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponseDto.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid input data",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied. Only CLIENT role can process payments.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> processPayment(@PathVariable UUID orderId, @RequestBody @Valid PaymentCreateDto dto) {
        log.info("REST request to process payment for order: {}", orderId);
        PaymentResponseDto response = paymentService.processPayment(orderId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm payment by ID", description = "Resource to confirm a payment. Only ADMIN can access.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment confirmed successfully"),
                    @ApiResponse(responseCode = "404", description = "Payment not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponseDto> confirmPayment(@PathVariable UUID id) {
        log.info("REST request to confirm payment: {}", id);
        PaymentResponseDto response = paymentService.confirmPayment(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "List payments by client", description = "Resource to list payments of the authenticated client with pagination",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
            })
    @GetMapping("/my-payments")
    public ResponseEntity<Page<PaymentResponseDto>> getMyPayments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to list payments for user: {}", authentication.getName());
        UUID clientId = UUID.fromString(authentication.getCredentials().toString());
        Page<PaymentResponseDto> payments = paymentService.findPaymentsByClientId(clientId, page, size);
        return ResponseEntity.ok(payments);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payment by ID", description = "Resource to get a specific payment by its ID. Only ADMIN can access.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Payment not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable UUID id) {
        log.info("REST request to get payment by id: {}", id);
        PaymentResponseDto payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payment by order ID", description = "Resource to get a payment by its order ID. Only ADMIN can access.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Payment not found for this order",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(@PathVariable UUID orderId) {
        log.info("REST request to get payment by order id: {}", orderId);
        PaymentResponseDto payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "Mercado Pago webhook", description = "Webhook endpoint to receive Mercado Pago payment notifications.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid webhook data")
            })
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody java.util.Map<String, Object> payload) {
        log.info("REST request received Mercado Pago webhook: {}", payload);

        if (payload.containsKey("data") && payload.get("data") instanceof java.util.Map data) {
            Object idObj = data.get("id");
            String topic = (String) payload.getOrDefault("type", "payment");

            if (idObj != null) {
                Long mpPaymentId = Long.valueOf(idObj.toString());
                paymentService.processWebhookNotification(mpPaymentId, topic);
                return ResponseEntity.ok().build();
            }
        }

        if (payload.containsKey("id") && payload.containsKey("topic")) {
            Long mpPaymentId = Long.valueOf(payload.get("id").toString());
            String topic = (String) payload.get("topic");
            paymentService.processWebhookNotification(mpPaymentId, topic);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.ok().build();
    }
}
