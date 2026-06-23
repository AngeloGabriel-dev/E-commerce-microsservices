package com.Ecommerce.Payment.mercadopago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class MercadoPagoDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentPreferenceRequest {
        private String externalReference;
        private String title;
        private BigDecimal totalAmount;
        private Integer quantity;
        private String currencyId;
        private String description;
        private Payer payer;
        private List<Item> items;
        private List<PaymentMethod> paymentMethods;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Payer {
            private String email;
            private String name;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Item {
            private String id;
            private String title;
            private String description;
            private String currencyId;
            private BigDecimal unitPrice;
            private Integer quantity;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PaymentMethod {
            private String id;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentPreferenceResponse {
        private String id;
        private String initPoint;
        private String sandboxInitPoint;
        private String externalReference;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentNotification {
        private String id;
        private String topic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MercadoPagoPaymentResponse {
        private Long id;
        private String status;
        private String statusDetail;
        private String externalReference;
        private BigDecimal transactionAmount;
        private String paymentTypeId;
        private String paymentMethodId;
        private String description;
        private Payer payer;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Payer {
            private String email;
            private String identification;
            private String name;
        }
    }
}