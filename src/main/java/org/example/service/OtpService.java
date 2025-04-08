package org.example.service;

import org.example.model.Operation;
import org.example.util.EmailNotificationService;

public class OtpService {

    public OtpService(EmailNotificationService emailService) {
    }

    void initiateOperation(Operation operation) {
        System.out.println("пока нет кода");
    }

    void completeOperation(Long operationId) {
        System.out.println("пока нет кода");
    }

    public void generateOtpCodeForUser(String mail) {
        System.out.println("Пока кода нет");
    }

}
