package org.example.api;

import org.example.dao.UserDao;
import org.example.model.User;
import org.example.service.OtpService;
import org.example.service.UserService;

import java.util.List;

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

        List<User> allUsers = userService.getAllUsers();

        if (!allUsers.isEmpty()) {
            System.out.println("Список пользователей:");
            for (User user : allUsers) {
                System.out.println(user.getId() + " " + user.getUsername() + " (" + user.getRole() + ")");
            }
        } else {
            System.out.println("Список пользователей пуст.");
        }
    }

}
