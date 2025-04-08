package org.example;


import org.example.api.AdminController;
import org.example.api.UserController;
import org.example.dao.UserDao;
import org.example.util.EmailNotificationService;
import org.example.service.OtpService;
import org.example.service.UserService;
import org.example.api.OperationController;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;

public class Main {

    public static void main(String[] args) {
        // Создание экземпляров сервисов и контроллеров
        EmailNotificationService emailService = new EmailNotificationService();
        OtpService otpService = new OtpService(emailService);
        TelegramBot telegramBot = new TelegramBot();
        UserDao userDao = new UserDao();
        SmppClient smsSender = new SmppClient();
        UserService userService = new UserService(userDao);

        UserController userController = new UserController(userService, otpService);
        OperationController operationController = new OperationController(otpService);
        AdminController adminController = new AdminController(userService, otpService);

        // Заглушка для тестирования контроллера
        userController.registerUser("John Doe", "john@example.com", "password");
        userController.loginUser("john@example.com", "password");

        // Тестирование отправки OTP-кода
        otpService.generateOtpCodeForUser("john@example.com");

        // Тестирование административной панели
        adminController.changeOtpConfig(10, 6); // Меняем время жизни и длину OTP-кода
        adminController.listUsers(); // Просматриваем список пользователей

        // Тестирование защищённых операций
        operationController.initiateProtectedOperation("Send Money", "john@example.com");
        operationController.verifyOtpCode("john@example.com", "123456"); // Проверка OTP-кода
        // operationController.initiateProtectedOperation(protectedOperation);

        // Завершение работы приложения
        System.out.println("Завершаем работу приложения.");
    }
}