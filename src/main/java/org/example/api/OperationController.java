package org.example.api;

import org.example.model.OtpCode;
import org.example.service.OtpService;
import org.example.model.Operation;

import java.time.LocalDateTime;

import static org.example.Main.getInput;

public class OperationController {

    private final OtpService otpService;

    public OperationController(OtpService otpService) {
        this.otpService = otpService;
    }

    public void initiateProtectedOperationToEmail(Operation operation) {

        //данные для генерации OTP кода
        String generatedOtpCode = otpService.generateOtpCode();
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expirationTime = currentTime.plusDays(5);

        //создан объект OTP кода
        OtpCode otpCode = new OtpCode(operation.getUserId(), operation.getId(), generatedOtpCode, "ACTIVE", currentTime, expirationTime, operation.getDescription());
        System.out.println(operation.getUserId());

        System.out.println("\nСгенерирован OTP-код: " + otpCode.getCode());

        System.out.print("\nЭлектронная почта: ");
        String email = getInput("");

        System.out.println("\nИнициирована защищённая операция: " + operation.getDescription());

        try {
            otpService.initiateOperationToEmail(email, otpCode);
            System.out.println("OTP-код отправлен на вашу электронную почту.");
        } catch (Exception e) {
            System.out.println("Ошибка генерации OTP-кода: " + e.getMessage());
        }

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