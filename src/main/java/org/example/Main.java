package org.example;

import org.example.api.AdminController;
import org.example.api.UserController;
import org.example.dao.OtpDao;
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
        TelegramBot telegramBot = new TelegramBot();
        UserDao userDao = new UserDao();
        OtpDao otpDao = new OtpDao();
        SmppClient smsSender = new SmppClient();
        EmailNotificationService emailService = new EmailNotificationService();
        UserService userService = new UserService(userDao);
        OtpService otpService = new OtpService(emailService, otpDao, smsSender);

        UserController userController = new UserController(userService, otpService);
        OperationController operationController = new OperationController(otpService);
        AdminController adminController = new AdminController(userService, otpService);

        System.out.println("Добро пожаловать! Пожалуйста, войдите в систему.");
        String name = getInput("Укажите имя: ");
        String password = getInput("Пароль: ");

        // обновление статусов OTP кодов при каждом входе в приложение
        operationController.processExpiredOtpCodes();

        try {
            // Логин пользователя
            userController.loginUser(name, password);

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
                    adminController.changeOtpConfig();
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
        User loggedInUser = null;

        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Зарегистрироваться");
            System.out.println("2. Войти");
            System.out.println("3. Иницировать операцию и отправить OTP-код");
            System.out.println("4. Проверить OTP-код");
            System.out.println("0. Выход");

            int choice = Integer.parseInt(getInput("Ваш выбор: "));

            switch (choice) {
                case 1:
                    userController.registerUser();
                    break;
                case 2:
                    String name = getInput("Укажите имя: ");
                    String password = getInput("Пароль: ");
                    loggedInUser = userController.loginUser(name, password);
                    break;
                case 3:

                    //данные для создания класса операция
                    System.out.print("ID операции: ");
                    long id = Long.parseLong(getInput(""));
                    System.out.print("Описание операции: ");
                    String description = getInput("");
                    long userid = loggedInUser.getId();


                    //создаем только операцию
                    Operation operation = new Operation(id, description, userid);

                    System.out.println("\nВыберите куда хотите направить OTP-код:");
                    System.out.println("1. Электронная почта");
                    System.out.println("2. Смс");
                    System.out.println("3. Телеграмм");
                    System.out.println("4. Сохранить в файл");
                    System.out.println("0. Выход");

                    int choice_ = Integer.parseInt(getInput("Ваш выбор: "));

                    switch(choice_) {
                        case 1:
                            operationController.initiateProtectedOperationToEmail(operation);
                            break;
                        case 2:
                            operationController.initiateProtectedOperationToSmpp(operation);
                            break;
                        case 3:
                            System.out.println("здесь будет код");
                            break;
                        case 4:
                            operationController.saveOtpCodeToFile(operation);
                            break;
                        case 0:
                            System.out.println("Выход.");
                            return;
                    }
                    break;
                case 4:
                    operationController.verifyOtpCode("john@example.com", "123456");
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


}

