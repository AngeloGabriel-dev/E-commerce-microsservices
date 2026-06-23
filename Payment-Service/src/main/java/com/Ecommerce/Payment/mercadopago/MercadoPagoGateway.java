package com.Ecommerce.Payment.mercadopago;

import com.Ecommerce.Payment.mercadopago.MercadoPagoDTOs.MercadoPagoPaymentResponse;
import com.Ecommerce.Payment.mercadopago.MercadoPagoDTOs.PaymentPreferenceRequest;
import com.Ecommerce.Payment.mercadopago.MercadoPagoDTOs.PaymentPreferenceResponse;

/**
 * Interface for payment gateway integration.
 * Implementations can use Mercado Pago or other providers.
 */
public interface MercadoPagoGateway {

    /**
     * Creates a payment preference (checkout) in the payment gateway.
     *
     * @param request the payment preference request details
     * @return the response with checkout URL and preference ID
     */
    PaymentPreferenceResponse createPreference(PaymentPreferenceRequest request);

    /**
     * Gets payment details from the payment gateway by payment ID.
     *
     * @param paymentId the Mercado Pago payment ID
     * @return the payment details from the gateway
     */
    MercadoPagoPaymentResponse getPayment(Long paymentId);

    /**
     * Processes a full refund for a payment.
     *
     * @param paymentId the Mercado Pago payment ID
     * @return true if refund was successful
     */
    boolean refundPayment(Long paymentId);
}