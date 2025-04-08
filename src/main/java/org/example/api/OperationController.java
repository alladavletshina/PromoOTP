package org.example.api;

import org.example.service.OtpService;
import org.example.model.Operation;

public class OperationController {

    private final OtpService otpService;

    public OperationController(OtpService otpService) {
        this.otpService = otpService;
    }

    public void initiateProtectedOperation(Operation operation) {
        System.out.println("Инициирована защищённая операция: " + operation.getDescription() + " для пользователя " + operation.getUserEmail());

        // Генерация OTP-кода для данной операции
        String generatedOtpCode = otpService.generateOtpCodeForUser(operation.getUserEmail());
        System.out.println("Сгенерирован OTP-код: " + generatedOtpCode);

        // Отправка OTP-кода через выбранный канал (например, Email/SMS/Telegram)
        otpService.sendOtpCodeToUser(operation.getUserEmail(), generatedOtpCode);
    }

    public void verifyOtpCode(String userEmail, String otpCode) {
        System.out.println("Проверка OTP-кода для пользователя " + userEmail + ". Код: " + otpCode);

        boolean isValid = otpService.checkOtpCode(userEmail, otpCode);
        if (isValid) {
            System.out.println("OTP-код верен. Операция разрешена.");
        } else {
            System.out.println("OTP-код неверен. Операция отклонена.");
        }
    }
}