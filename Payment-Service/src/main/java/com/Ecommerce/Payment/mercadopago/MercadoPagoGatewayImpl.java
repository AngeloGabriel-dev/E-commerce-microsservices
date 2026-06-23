package com.Ecommerce.Payment.mercadopago;

import com.Ecommerce.Payment.mercadopago.MercadoPagoDTOs.MercadoPagoPaymentResponse;
import com.Ecommerce.Payment.mercadopago.MercadoPagoDTOs.PaymentPreferenceRequest;
import com.Ecommerce.Payment.mercadopago.MercadoPagoDTOs.PaymentPreferenceResponse;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentRefund;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MercadoPagoGatewayImpl implements MercadoPagoGateway {

    @Override
    public PaymentPreferenceResponse createPreference(PaymentPreferenceRequest request) {
        log.info("Creating Mercado Pago preference for external reference: {}", request.getExternalReference());

        try {
            PreferenceClient client = new PreferenceClient();

            PreferenceRequest preferenceRequest = buildPreferenceRequest(request);
            Preference preference = client.create(preferenceRequest);

            log.info("Preference created successfully with id: {}", preference.getId());

            return PaymentPreferenceResponse.builder()
                    .id(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .externalReference(request.getExternalReference())
                    .status("created")
                    .build();

        } catch (MPApiException e) {
            log.error("Mercado Pago API error: status={}, content={}",
                    e.getApiResponse().getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("Failed to create Mercado Pago preference: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Mercado Pago error: {}", e.getMessage());
            throw new RuntimeException("Failed to create Mercado Pago preference", e);
        }
    }

    @Override
    public MercadoPagoPaymentResponse getPayment(Long paymentId) {
        log.info("Getting Mercado Pago payment details for id: {}", paymentId);

        try {
            com.mercadopago.client.payment.PaymentClient client =
                    new com.mercadopago.client.payment.PaymentClient();

            Payment payment = client.get(paymentId);

            return MercadoPagoPaymentResponse.builder()
                    .id(payment.getId())
                    .status(payment.getStatus())
                    .statusDetail(payment.getStatusDetail())
                    .externalReference(payment.getExternalReference())
                    .transactionAmount(payment.getTransactionAmount())
                    .paymentTypeId(payment.getPaymentTypeId())
                    .paymentMethodId(payment.getPaymentMethodId())
                    .description(payment.getDescription())
                    .payer(Optional.ofNullable(payment.getPayer())
                            .map(p -> MercadoPagoPaymentResponse.Payer.builder()
                                    .email(p.getEmail())
                                    .name(p.getFirstName())
                                    .build())
                            .orElse(null))
                    .build();

        } catch (MPApiException e) {
            log.error("Mercado Pago API error getting payment: status={}, content={}",
                    e.getApiResponse().getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("Failed to get Mercado Pago payment: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Mercado Pago error getting payment: {}", e.getMessage());
            throw new RuntimeException("Failed to get Mercado Pago payment", e);
        }
    }

    @Override
    public boolean refundPayment(Long paymentId) {
        log.info("Refunding Mercado Pago payment with id: {}", paymentId);

        try {
            com.mercadopago.client.payment.PaymentClient client =
                    new com.mercadopago.client.payment.PaymentClient();

            PaymentRefund refundedPayment = client.refund(paymentId);

            boolean success = "refunded".equals(refundedPayment.getStatus());
            log.info("Payment {} refunded successfully: {}", paymentId, success);
            return success;

        } catch (MPApiException e) {
            log.error("Mercado Pago API error refunding payment: status={}, content={}",
                    e.getApiResponse().getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("Failed to refund Mercado Pago payment: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Mercado Pago error refunding payment: {}", e.getMessage());
            throw new RuntimeException("Failed to refund Mercado Pago payment", e);
        }
    }

    private PreferenceRequest buildPreferenceRequest(PaymentPreferenceRequest request) {
        List<PreferenceItemRequest> items = Optional.ofNullable(request.getItems())
                .orElseGet(ArrayList::new)
                .stream()
                .map(item -> PreferenceItemRequest.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .currencyId(item.getCurrencyId())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        if (items.isEmpty() && request.getTotalAmount() != null) {
            items.add(PreferenceItemRequest.builder()
                    .title(request.getTitle() != null ? request.getTitle() : "Payment")
                    .description(request.getDescription())
                    .currencyId(request.getCurrencyId() != null ? request.getCurrencyId() : "BRL")
                    .unitPrice(request.getTotalAmount())
                    .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                    .build());
        }
        /*
        PreferencePaymentMethodsRequest paymentMethods = Optional.ofNullable(request.getPaymentMethods())
                .orElseGet(ArrayList::new)
                .stream()
                .map(pm -> PreferencePaymentMethodsRequest.builder()

                        .installments()
                        .build())
                .collect(Collectors.toList());
        */
        PreferencePayerRequest payer = Optional.ofNullable(request.getPayer())
                .map(p -> PreferencePayerRequest.builder()
                        .email(p.getEmail())
                        .name(p.getName())
                        .build())
                .orElse(null);

        return PreferenceRequest.builder()
                .externalReference(request.getExternalReference())
                .items(items)
                .payer(payer)
                //.paymentMethods(paymentMethods.isEmpty() ? null : paymentMethods)
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(request.getExternalReference() + "/success")
                        .failure(request.getExternalReference() + "/failure")
                        .pending(request.getExternalReference() + "/pending")
                        .build())
                .autoReturn("approved")
                .build();
    }
}