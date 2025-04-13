package org.example;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.example.api.UserController;
import org.example.dao.OtpDao;
import org.example.dao.UserDao;
import org.example.util.EmailNotificationService;
import org.example.service.OtpService;
import org.example.service.UserService;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

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

        System.out.println("Добро пожаловать! Выберите действие:");
        System.out.println("1. Зарегистрироваться");
        System.out.println("2. Войти");

        int choice = Integer.parseInt(getInput("Ваш выбор: "));

        switch (choice) {
            case 1:
                callRegisterUserApi();
                break;
            case 2:
                System.out.println("Пожалуйста, войдите в систему.");
                String name = getInput("Укажите имя: ");
                String password = getInput("Пароль: ");

                JSONObject userResponse = callLoginUserApi(name, password);
                if (userResponse != null) {
                    String username = userResponse.getString("username");
                    long userId = userResponse.getLong("user_id");
                    String role = userResponse.optString("role", "<no_role>");
                    String token = generateJwtToken(username);

                    if (token != null) {
                        System.out.println("Успешный вход. Ваш токен: " + token);

                        if (Objects.equals(role, "ADMIN")) {
                            runAdminInterface(otpService, token);
                        } else {
                            runUserInterface(userId, otpService, token);
                        }
                    } else {
                        System.out.println("Ошибка входа: Неправильное имя пользователя или пароль.");
                    }

                } else {
                    System.out.println("Ошибка входа.");
                }

                break;
            default:
                System.out.println("Неправильный выбор. Попробуйте снова.");
        }
    }

    // Метод для запуска интерфейса администратора
    private static void runAdminInterface(OtpService otpService, String token) {

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
                        callChangeOtpConfigApi(token);
                    } else {
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 2:
                    if (checkTokenValidity(token)) {
                        callListUsersApi(token);
                    } else {
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 3:
                    if (checkTokenValidity(token)) {
                        callDeleteUserApi(token);
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
    private static void runUserInterface(long userId, OtpService otpService, String token) {

        //старт планировщика
        otpService.initScheduler();

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
                        long id_operation = Long.parseLong(getInput(""));
                        System.out.print("Описание операции: ");
                        String description = getInput("");


                        System.out.println("\nВыберите куда хотите направить OTP-код:");
                        System.out.println("1. Электронная почта");
                        System.out.println("2. СМС");
                        System.out.println("3. Телеграмм");
                        System.out.println("4. Сохранить в файл");
                        System.out.println("0. Выход");

                        int choice_ = Integer.parseInt(getInput("Ваш выбор: "));

                        switch (choice_) {
                            case 1:
                                callInitiateOperationApi(id_operation, description, userId, "email", token);
                                break;
                            case 2:
                                callInitiateOperationApi(id_operation, description, userId, "sms", token);
                                break;
                            case 3:
                                System.out.println("Здесь будет код для отправки в Telegram");
                                break;
                            case 4:
                                callInitiateOperationApi(id_operation, description, userId, "file", token);
                                break;
                            case 0:
                                // Остановка планировщика задач
                                otpService.shutdown();

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

                        //Получаем от пользователя OTP код
                        System.out.print("OTP-код: ");
                        String code = getInput("");

                        callVerifyOtpCodeApi(code, token);
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

    // Вспомогательные методы для взаимодействия с пользователем
    public static String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static String generateJwtToken(String username) {
        // Пример генерации JWT-токена
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(username); // Установим субъект (идентификатор пользователя)
        claims.setIssuedAt(now);              // Установим время выпуска токена
        claims.setExpiration(new Date(nowMillis + 3600000)); // Установим срок действия токена (1 час)

        // Сигнатурный ключ для подписания токена
        String secretKey = "mySecretKey"; // Замените на реальный секретный ключ!

        // Строим и подписываем токен
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
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

    // Вызов API для изменения конфигурации OTP
    private static void callChangeOtpConfigApi(String token) {

        System.out.println("Укажите новую конфигурацию OTP-кодов:");

        System.out.print("Длина OTP-кода: ");
        int codeLength = Integer.parseInt(getInput(""));

        System.out.print("Время жизни OTP-кода (минуты): ");
        int lifetimeInMinutes = Integer.parseInt(getInput(""));

        try {
            URL url = new URL("http://localhost:8000/admin/configure-otp");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);

            // Отправка параметров конфигурации OTP
            OutputStream os = conn.getOutputStream();
            String payload = "codeLength=" + codeLength + "&lifetimeInMinutes=" + lifetimeInMinutes;
            os.write(payload.getBytes());
            os.flush();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Конфигурация OTP обновлена успешно.");
            } else {
                System.out.println("Ошибка обновления конфигурации OTP: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с API: " + e.getMessage());
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

    // Вызов API для удаления пользователя (администратор)
    private static void callDeleteUserApi(String token) {
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

    // Вызов API для удаления пользователя (пользователь)
    private static void callRegisterUserApi() {

        String username = getInput("Укажите имя: ");
        String password = getInput("Пароль: ");

        try {
            URL url = new URL("http://localhost:9000/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Создаем JSON для отправки
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            // Отправляем данные
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();

            // Читаем ответ
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Пользователь зарегистрирован успешно.");
            } else {
                System.out.println("Ошибка регистрации: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }

    // Вызов API для входа пользователя (пользователь)
    private static JSONObject callLoginUserApi(String username, String password) {
        try {
            URL url = new URL("http://localhost:9000/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Создаем JSON для отправки
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            // Отправляем данные
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();

            // Читаем ответ
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                // Предполагается, что API возвращает объект User в формате JSON
                return jsonResponse.getJSONObject("user"); // Возвращаем объект User
            } else {
                System.out.println("Ошибка входа: " + conn.getResponseMessage());
                return null; // Возвращаем null, если ответ не OK
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с API: " + e.getMessage());
            return null; // Возвращаем null, если возникла ошибка
        }
    }

    // Вызов API для генерации кода OTP (пользователь)
    private static void callInitiateOperationApi(long operationId, String description, long userId, String channel, String token) {
        try {
            URL url = new URL("http://localhost:9000/initiate-operation");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);

            // Создаем JSON для отправки
            JSONObject json = new JSONObject();
            json.put("id", operationId);
            json.put("description", description);
            json.put("user_id", userId);
            json.put("channel", channel);

            // Отправляем данные
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();

            // Читаем ответ
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Операция инициирована успешно.");
            } else {
                System.out.println("Ошибка инициирования операции: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }

    // Вызов API для проверки OTP кода (пользователь)

    private static void callVerifyOtpCodeApi(String code, String token) {
        try {
            URL url = new URL("http://localhost:9000/verify-otp-code");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoOutput(true);

            // Создаем JSON для отправки
            JSONObject json = new JSONObject();
            json.put("code", code);

            // Отправляем данные
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();

            // Читаем ответ
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                String message = jsonResponse.getString("message");
                System.out.println(message);
            } else {
                System.out.println("Ошибка проверки OTP-кода: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }
}

