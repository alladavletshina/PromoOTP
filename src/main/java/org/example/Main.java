package org.example;


import org.example.api.AdminController;
import org.example.api.UserController;
import org.example.dao.UserDao;
import org.example.model.Operation;
import org.example.util.EmailNotificationService;
import org.example.service.OtpService;
import org.example.service.UserService;
import org.example.api.OperationController;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // Создание объектов сервисов и контроллеров
        EmailNotificationService emailService = new EmailNotificationService();
        TelegramBot telegramBot = new TelegramBot();
        UserDao userDao = new UserDao();
        SmppClient smsSender = new SmppClient();
        UserService userService = new UserService(userDao);
        OtpService otpService = new OtpService(emailService, smsSender, telegramBot);

        UserController userController = new UserController(userService, otpService);
        OperationController operationController = new OperationController(otpService);
        AdminController adminController = new AdminController(userService, otpService);

        while (true) { // Бесконечный цикл для постоянного взаимодействия с пользователем
            System.out.println("\nВыберите действие:");
            System.out.println("1. Зарегистрироваться");
            System.out.println("2. Войти");
            System.out.println("3. Сгенерировать OTP-код");
            System.out.println("4. Проверить OTP-код");
            System.out.println("5. Запустить защищённую операцию");
            System.out.println("6. Настроить конфигурацию OTP-кодов (администрация)");
            System.out.println("7. Вывести список пользователей (администрация)");
            System.out.println("0. Выход");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    userController.registerUser("John Doe", "john@example.com", "password");
                    break;
                case 2:
                    userController.loginUser("john@example.com", "password");
                    break;
                case 3:
                    otpService.generateOtpCodeForUser("john@example.com");
                    break;
                case 4:
                    operationController.verifyOtpCode("john@example.com", "123456");
                    break;
                case 5:
                    Operation protectedOperation = new Operation(111111111L,"Снятие", 1L);
                    operationController.initiateProtectedOperation(protectedOperation);
                    break;
                case 6:
                    adminController.changeOtpConfig(10, 6);
                    break;
                case 7:
                    adminController.listUsers();
                    break;
                case 0:
                    System.out.println("Завершаем работу приложения.");
                    return;
                default:
                    System.out.println("Неправильный выбор. Попробуйте снова.");
            }
        }
    }
}