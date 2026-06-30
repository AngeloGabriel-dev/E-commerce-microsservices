package com.Ecommerce.Notification.service;

import com.Ecommerce.common.kafka.event.order.OrderCancelledEvent;
import com.Ecommerce.common.kafka.event.order.OrderConfirmedEvent;
import com.Ecommerce.common.kafka.event.payment.PaymentConfirmedEvent;
import com.Ecommerce.common.kafka.event.payment.PaymentExpiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;

    public void notifyOrderConfirmed(OrderConfirmedEvent event, String userEmail, String userPhone) {
        String subject = "Pedido Confirmado - E-commerce";
        Map<String, Object> variables = Map.of(
                "orderId", event.orderId().toString(),
                "type", "confirmado"
        );

        emailService.sendEmail(userEmail, subject, "order-notification", variables);
        smsService.sendSms(userPhone, "Seu pedido " + event.orderId() + " foi confirmado!");
        log.info("Notification sent for order confirmed: {}", event.orderId());
    }

    public void notifyOrderCancelled(OrderCancelledEvent event, String userEmail, String userPhone) {
        String subject = "Pedido Cancelado - E-commerce";
        Map<String, Object> variables = Map.of(
                "orderId", event.orderId().toString(),
                "type", "cancelado",
                "reason", event.reason()
        );

        emailService.sendEmail(userEmail, subject, "order-notification", variables);
        smsService.sendSms(userPhone, "Seu pedido " + event.orderId() + " foi cancelado. Motivo: " + event.reason());
        log.info("Notification sent for order cancelled: {}", event.orderId());
    }

    public void notifyPaymentConfirmed(PaymentConfirmedEvent event, String userEmail, String userPhone) {
        String subject = "Pagamento Confirmado - E-commerce";
        Map<String, Object> variables = Map.of(
                "orderId", event.orderId().toString(),
                "totalPrice", event.totalPrice().toString(),
                "type", "confirmado"
        );

        emailService.sendEmail(userEmail, subject, "payment-notification", variables);
        smsService.sendSms(userPhone, "Pagamento do pedido " + event.orderId() + " confirmado! Valor: R$" + event.totalPrice());
        log.info("Notification sent for payment confirmed: {}", event.orderId());
    }

    public void notifyPaymentExpired(PaymentExpiredEvent event, String userEmail, String userPhone) {
        String subject = "Pagamento Expirado - E-commerce";
        Map<String, Object> variables = Map.of(
                "orderId", event.orderId().toString(),
                "totalPrice", event.totalPrice().toString(),
                "type", "expirado"
        );

        emailService.sendEmail(userEmail, subject, "payment-notification", variables);
        smsService.sendSms(userPhone, "O pagamento do pedido " + event.orderId() + " expirou.");
        log.info("Notification sent for payment expired: {}", event.orderId());
    }
}