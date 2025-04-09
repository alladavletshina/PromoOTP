package org.example.api;

import org.example.service.OtpService;
import org.example.service.UserService;

public class UserController {

    private final UserService userService;
    private final OtpService otpService;

    public UserController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    public void registerUser(String name, String email, String password) {
        System.out.println("Регистрация пользователя: " + name + ", email: " + email);
        // Логика регистрации пользователя
    }

    public void loginUser(String name, String password) {
        System.out.println("Авторизация пользователя: " + name);
        // Логика авторизации пользователя
    }
}