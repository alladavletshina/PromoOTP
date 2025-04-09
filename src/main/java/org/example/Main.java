package org.example;

import org.example.api.AdminController;
import org.example.api.UserController;
import org.example.dao.UserDao;
import org.example.model.Operation;
import org.example.model.User;
import org.example.util.EmailNotificationService;
import org.example.service.OtpService;
import org.example.service.UserService;
import org.example.api.OperationController;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static OtpService otpService;
    private static UserService userService;

    public static void main(String[] args) {

        // Создание общих объектов сервисов и контроллеров
        EmailNotificationService emailService = new EmailNotificationService();
        TelegramBot telegramBot = new TelegramBot();
        UserDao userDao = new UserDao();
        SmppClient smsSender = new SmppClient();
        UserService userService = new UserService(userDao);
        OtpService otpService = new OtpService(emailService, smsSender, telegramBot);

        UserController userController = new UserController(userService, otpService);
        OperationController operationController = new OperationController(otpService);
        AdminController adminController = new AdminController(userService, otpService);

        System.out.println("Добро пожаловать! Пожалуйста, войдите в систему.");
        String name = getInput("Укажите имя: ");
        String password = getInput("Пароль: ");

        try {
            // Логин пользователя
            userController.loginUser(name, password);
            System.out.println("Успешный вход.");

            // Определение роли пользователя
            User.Role role = userService.getRole(name);
            if (role == User.Role.ADMIN) {
                runAdminInterface(adminController);
            } else {
                runUserInterface(userController, operationController);
            }
        } catch (Exception e) {
            System.out.println("Ошибка входа: " + e.getMessage());
        }
    }

    // Метод для запуска интерфейса администратора
    private static void runAdminInterface(AdminController adminController) {
        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Изменить конфигурацию OTP-кодов");
            System.out.println("2. Получить список пользователей");
            System.out.println("3. Удалить пользователя");
            System.out.println("0. Выход");

            int choice = Integer.parseInt(getInput("Ваш выбор: "));

            switch (choice) {
                case 1:
                    changeOtpConfig(adminController);
                    break;
                case 2:
                    adminController.listUsers();
                    break;
                case 3:
                    adminController.deleteUser();
                    break;
                case 0:
                    System.out.println("Завершаем работу приложения.");
                    return;
                default:
                    System.out.println("Неправильный выбор. Попробуйте снова.");
            }
        }
    }

    // Метод для запуска интерфейса пользователя
    private static void runUserInterface(UserController userController, OperationController operationController) {
        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Зарегистрироваться");
            System.out.println("2. Войти");
            System.out.println("3. Сгенерировать OTP-код");
            System.out.println("4. Проверить OTP-код");
            System.out.println("5. Выполнить защищённую операцию");
            System.out.println("0. Выход");

            int choice = Integer.parseInt(getInput("Ваш выбор: "));

            switch (choice) {
                case 1:
                    userController.registerUser();
                    break;
                case 2:
                    //loginUser(userController);
                    userController.loginUser("john@example.com", "password");

                    break;
                case 3:
                    //generateOtpCode(otpService);
                    otpService.generateOtpCodeForUser("john@example.com");
                    break;
                case 4:
                    //verifyOtpCode(operationController);
                    operationController.verifyOtpCode("john@example.com", "123456");
                    break;
                case 5:
                    initiateProtectedOperation(operationController);
                    break;
                case 0:
                    System.out.println("Завершаем работу приложения.");
                    return;
                default:
                    System.out.println("Неправильный выбор. Попробуйте снова.");
            }
        }
    }

    // Вспомогательные методы для взаимодействия с пользователем
    public static String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static void changeOtpConfig(AdminController controller) {
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

    private static void generateOtpCode(OtpService service) {
        System.out.print("Электронная почта: ");
        String email = getInput("");

        try {
            service.generateOtpCodeForUser(email);
            System.out.println("OTP-код отправлен на вашу электронную почту.");
        } catch (Exception e) {
            System.out.println("Ошибка генерации OTP-кода: " + e.getMessage());
        }
    }


    private static void initiateProtectedOperation(OperationController controller) {
        System.out.print("ID операции: ");
        long id = Long.parseLong(getInput(""));
        System.out.print("Тип операции: ");
        String type = getInput("");
        System.out.print("Сумма: ");
        long amount = Long.parseLong(getInput(""));

        Operation protectedOperation = new Operation(id, type, amount);
        try {
            controller.initiateProtectedOperation(protectedOperation);
            System.out.println("Операция начата успешно.");
        } catch (Exception e) {
            System.out.println("Ошибка начала операции: " + e.getMessage());
        }
    }

}

