package com.Ecommerce.Notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    public void sendSms(String phoneNumber, String message) {
        // TODO: Integrate with a real SMS provider (e.g., Twilio, Amazon SNS)
        // For now, we log the SMS as a placeholder
        log.info("=== SMS SENT ===");
        log.info("To: {}", phoneNumber);
        log.info("Message: {}", message);
        log.info("=================");
    }
}