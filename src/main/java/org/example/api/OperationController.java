package org.example.api;

import org.example.service.OtpService;

public class OperationController {

    private final OtpService otpService;

    public OperationController(OtpService otpService) {
        this.otpService = otpService;
    }

    public void initiateProtectedOperation(String operationDescription, String userEmail) {
        System.out.println("Инициирована защищённая операция: " + operationDescription + " для пользователя " + userEmail);
        // Логика инициирования защищённой операции
    }

    public void verifyOtpCode(String userEmail, String otpCode) {
        System.out.println("Проверка OTP-кода для пользователя " + userEmail + ". Код: " + otpCode);
        // Логика проверки OTP-кода
    }
}