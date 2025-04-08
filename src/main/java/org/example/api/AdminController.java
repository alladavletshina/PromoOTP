package org.example.api;

import org.example.service.OtpService;
import org.example.service.UserService;

public class AdminController {

    private final UserService userService;
    private final OtpService otpService;

    public AdminController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    public void changeOtpConfig(int lifetimeInMinutes, int codeLength) {
        System.out.println("Изменена конфигурация OTP-кодов: время жизни " + lifetimeInMinutes +
                " минут, длина кода " + codeLength + " символов.");
        // Логика изменения конфигурации OTP-кодов
    }

    public void listUsers() {
        System.out.println("Список пользователей:");
        // Логика получения списка пользователей
    }
}
