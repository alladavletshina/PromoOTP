package org.example.api;

import org.example.model.User;
import org.example.service.OtpService;
import org.example.service.UserService;

import static org.example.Main.getInput;

public class UserController {

    private final UserService userService;
    private final OtpService otpService;

    public UserController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    public void registerUser() {
        System.out.print("Имя пользователя: ");
        String username = getInput("");
        System.out.print("Пароль: ");
        String password = getInput("");

        User user = new User(username,password, User.Role.USER);

        try {
            if (userService.addUser(user)) {
                System.out.println("Пользователь зарегистрирован успешно.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
    }

    public User loginUser(String name, String password) {
        try {
            // Найденный пользователь
            User user = userService.getUserByUsername(name, password);
            if (user != null) {
                System.out.println("Успешный вход.");
                return user; // Возвращаем найденного пользователя
            } else {
                System.out.println("Пользователь не найден.");
                return null; // Возвращаем null, если пользователь не найден
            }
        } catch (Exception e) {
            System.out.println("Ошибка входа: " + e.getMessage());
            return null; // Возвращаем null в случае ошибки
        }
    }
}