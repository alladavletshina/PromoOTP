package org.example;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.example.api.AdminController;
import org.example.api.UserController;
import org.example.dao.OtpDao;
import org.example.dao.UserDao;
import org.example.model.Operation;
import org.example.model.User;
import org.example.util.EmailNotificationService;
import org.example.service.OtpService;
import org.example.service.UserService;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

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
        AdminController adminController = new AdminController(userService, otpService);

        System.out.println("Добро пожаловать! Выберите действие:");
        System.out.println("1. Зарегистрироваться");
        System.out.println("2. Войти");

        int choice = Integer.parseInt(getInput("Ваш выбор: "));

        switch (choice) {
            case 1:
                userController.registerUser();
                break;
            case 2:
                System.out.println("Пожалуйста, войдите в систему.");
                String name = getInput("Укажите имя: ");
                String password = getInput("Пароль: ");

                // Логин пользователя
                String token = userController.getUserToken(name, password);
                User loggedInUser = userController.loginUser(name, password);

                if (token != null) {
                    System.out.println("Успешный вход. Ваш токен: " + token);

                    // Определение роли пользователя
                    User.Role role = userService.getRole(name);

                    if (role == User.Role.ADMIN) {
                        runAdminInterface(adminController, otpService, token);
                    } else {
                        runUserInterface(userController, token, loggedInUser);
                    }
                } else {
                    System.out.println("Ошибка входа: Неправильное имя пользователя или пароль.");
                }
                break;
            default:
                System.out.println("Неправильный выбор. Попробуйте снова.");
        }
    }

    // Метод для запуска интерфейса администратора
    private static void runAdminInterface(AdminController adminController, OtpService otpService, String token) {
        //старт планировщика
        otpService.initScheduler();

        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Изменить конфигурацию OTP-кодов");
            System.out.println("2. Получить список пользователей");
            System.out.println("3. Удалить пользователя");
            System.out.println("0. Выход");

            int choice = Integer.parseInt(getInput("Ваш выбор: "));

            switch (choice) {
                case 1:
                    if (checkTokenValidity(token)) {
                        adminController.changeOtpConfig(token);
                    } else {
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 2:
                    if (checkTokenValidity(token)) {
                        //adminController.listUsers(token);
                        callListUsersApi(token);
                    } else {
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 3:
                    if (checkTokenValidity(token)) {
                        adminController.deleteUser(token);
                    } else {
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 0:
                    // Остановка планировщика задач
                    otpService.shutdown();
                    System.out.println("Завершаем работу приложения.");
                    return;
                default:
                    System.out.println("Неправильный выбор. Попробуйте снова.");
            }
        }
    }

    // Метод для запуска интерфейса пользователя
    private static void runUserInterface(UserController userController, String token, User loggedInUser) {

        //старт планировщика
        userController.initScheduler();

        while (true) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Иницировать операцию и отправить OTP-код");
            System.out.println("2. Проверить OTP-код");
            System.out.println("0. Выход");

            int choice = Integer.parseInt(getInput("Ваш выбор: "));

            switch (choice) {
                case 1:
                    if (checkTokenValidity(token)) {
                        // Данные для создания операции
                        System.out.print("ID операции: ");
                        long id = Long.parseLong(getInput(""));
                        System.out.print("Описание операции: ");
                        String description = getInput("");

                        // Создаем операцию
                        Operation operation = new Operation(id, description, loggedInUser.getId());

                        System.out.println("\nВыберите куда хотите направить OTP-код:");
                        System.out.println("1. Электронная почта");
                        System.out.println("2. СМС");
                        System.out.println("3. Телеграмм");
                        System.out.println("4. Сохранить в файл");
                        System.out.println("0. Выход");

                        int choice_ = Integer.parseInt(getInput("Ваш выбор: "));

                        switch (choice_) {
                            case 1:
                                userController.initiateProtectedOperationToEmail(operation, token);
                                break;
                            case 2:
                                userController.initiateProtectedOperationToSmpp(operation, token);
                                break;
                            case 3:
                                System.out.println("Здесь будет код для отправки в Telegram");
                                break;
                            case 4:
                                userController.saveOtpCodeToFile(operation, token);
                                break;
                            case 0:
                                userController.shutdown();
                                System.out.println("Выход.");
                                return;
                            default:
                                System.out.println("Неправильный выбор. Попробуйте снова.");
                        }
                    } else {
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 2:
                    if (checkTokenValidity(token)) {
                        userController.verifyOtpCode(token);
                    } else {
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 0:
                    // Остановка планировщика задач
                    userController.shutdown();
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

    private static boolean checkTokenValidity(String token) {

        String secretKey = "mySecretKey";

        try {
            // Парсим токен и получаем полезные данные (claims)
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            // Извлекаем дату истечения срока действия токена
            Date expiration = claims.getExpiration();

            // Проверяем, не истек ли токен
            if (new Date().before(expiration)) {
                return true; // Токен действителен
            } else {
                return false; // Токен истек
            }
        } catch (Exception e) {
            // Если произошла ошибка при разборе токена, считаем его недействительным
            System.out.println("Ошибка при проверке токена: " + e.getMessage());
            return false;
        }
    }

    // Вызов API для получения списка пользователей
    private static void callListUsersApi(String token) {
        try {
            URL url = new URL("http://localhost:8000/admin/list-users");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                System.out.println("Список пользователей:\n" + response.toString());
            } else {
                System.out.println("Ошибка получения списка пользователей: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }

    // Вызов API для удаления пользователя
    private void callDeleteUserApi(String token) {
        try {
            System.out.print("Введите id пользователя для удаления: ");
            long userId = Long.parseLong(getInput(""));

            URL url = new URL("http://localhost:8000/admin/delete-user/" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Пользователь удален успешно.");
            } else {
                System.out.println("Ошибка удаления пользователя: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }
}

