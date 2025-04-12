package org.example.api;

import org.example.dao.UserDao;
import org.example.model.OtpCode;
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

    public void changeOtpConfig(String token) {

        System.out.println("Укажите новую конфигурацию OTP-кодов:");

        System.out.print("Длина OTP-кода: ");
        int codeLength = Integer.parseInt(getInput(""));

        System.out.print("Время жизни OTP-кода (минуты): ");
        int lifetimeInMinutes = Integer.parseInt(getInput(""));

        try {
            otpService.changeOtpConfig(codeLength, lifetimeInMinutes);
        } catch (Exception e) {
            System.out.println("Ошибка обновления конфигурации: " + e.getMessage());
        }
    }

    public void listUsers(String token) {

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

    public void deleteUser(String token) {
        System.out.print("Введите id пользователя для удаления: ");
        long id = Long.parseLong(getInput(""));

        try {
            userService.deleteUser(id);
            System.out.println("Пользователь удалён.");
        } catch (Exception e) {
            System.out.println("Ошибка удаления пользователя: " + e.getMessage());
        }
    }

    public void processExpiredOtpCodes() {
        List<OtpCode> otpCodesNew = otpService.findAllOtpCodes();
        otpService.processExpiredOtpCodes(otpCodesNew);
    }

    public void initScheduler() {
        otpService.initScheduler();
    }

    public void shutdown() {
        otpService.shutdown();
    }

}
