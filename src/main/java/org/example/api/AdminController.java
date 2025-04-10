package org.example.api;

import org.example.dao.UserDao;
import org.example.model.User;
import org.example.service.OtpService;
import org.example.service.UserService;

import java.util.List;

import static org.example.Main.getInput;
import static org.example.model.User.Role.USER;

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
                if (user.getRole() == USER) {
                    System.out.println(user.getId() + " " + user.getUsername() + " (" + user.getRole() + ")");
                }
            }
        } else {
            System.out.println("Список пользователей пуст.");
        }
    }

    public void deleteUser() {
        System.out.print("Введите id пользователя для удаления: ");
        long id = Long.parseLong(getInput(""));

        try {
            userService.deleteUser(id);
            System.out.println("Пользователь удалён.");
        } catch (Exception e) {
            System.out.println("Ошибка удаления пользователя: " + e.getMessage());
        }
    }

    private void changeOtpConfig(AdminController controller) {
        System.out.print("Время жизни OTP-кода (минуты): ");
        int timeToLive = Integer.parseInt(getInput(""));
        System.out.print("Длина OTP-кода: ");
        int length = Integer.parseInt(getInput(""));

        try {
            controller.changeOtpConfig(timeToLive, length);
            System.out.println("Конфигурация OTP-кодов обновлена.");
        } catch (Exception e) {
            System.out.println("Ошибка обновления конфигурации: " + e.getMessage());
        }
    }

}
