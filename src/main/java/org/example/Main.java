package org.example;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.example.dao.OtpDao;
import org.example.dao.UserDao;
import org.example.service.OtpService;
import org.example.service.UserService;
import org.example.util.EmailNotificationService;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Приложение запущено!");

        // Создание общих объектов сервисов и контроллеров
        TelegramBot telegramBot = new TelegramBot();
        UserDao userDao = new UserDao();
        OtpDao otpDao = new OtpDao();
        SmppClient smsSender = new SmppClient();
        EmailNotificationService emailService = new EmailNotificationService();
        UserService userService = new UserService(userDao);
        OtpService otpService = new OtpService(emailService, otpDao, smsSender, telegramBot);


        System.out.println("Добро пожаловать! Выберите действие:");
        System.out.println("1. Зарегистрироваться");
        System.out.println("2. Войти");

        int choice = Integer.parseInt(getInput("Ваш выбор: "));

        switch (choice) {
            case 1:
                logger.info("Начало процесса регистрации пользователя");
                callRegisterUserApi();
                break;
            case 2:
                logger.info("Начало процесса авторизации пользователя");
                System.out.println("\nПожалуйста, войдите в систему.");
                String name = getInput("Укажите имя: ");
                String password = getInput("Пароль: ");

                JSONObject userResponse = callLoginUserApi(name, password);
                if (userResponse != null) {
                    String username = userResponse.getString("username");
                    long userId = userResponse.getLong("user_id");
                    String role = userResponse.optString("role", "<no_role>");
                    String token = generateJwtToken(username);

                    if (token != null) {
                        logger.info("Авторизация прошла успешно. Пользователь: {}", username);

                        if (Objects.equals(role, "ADMIN")) {
                            runAdminInterface(otpService, token);
                        } else {
                            runUserInterface(userId, otpService, token);
                        }
                    } else {
                        logger.warn("Ошибка авторизации: неправильное имя пользователя.");
                        System.out.println("Неправильное имя пользователя или пароль");
                    }

                } else {
                    logger.warn("Ошибка авторизации: пустое значение ответа от сервера");
                    System.out.println("Ошибка входа.");
                }

                break;
            default:
                logger.warn("Неверный выбор меню: {}", choice);
                System.out.println("Неправильный выбор. Попробуйте снова.");
        }
    }

    // Метод для запуска интерфейса администратора
    private static void runAdminInterface(OtpService otpService, String token) {
        logger.info("Администраторский интерфейс запущен");

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
                        logger.info("Изменение настроек OTP-кода администратором");
                        callChangeOtpConfigApi(token);
                    } else {
                        logger.warn("Неудачная попытка изменить настройки OTP-кода: токен недействителен");
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 2:
                    if (checkTokenValidity(token)) {
                        logger.info("Администратор запрашивает список пользователей");
                        callListUsersApi(token);
                    } else {
                        logger.warn("Неудачный запрос списка пользователей: токен недействителен");
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 3:
                    if (checkTokenValidity(token)) {
                        logger.info("Удаление пользователя администратором");
                        callDeleteUserApi(token);
                    } else {
                        logger.warn("Неудачное удаление пользователя: токен недействителен");
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 0:
                    // Остановка планировщика задач
                    otpService.shutdown();
                    logger.info("Администратор завершил сессию");
                    System.out.println("Завершаем работу приложения.");
                    return;
                default:
                    logger.warn("Некорректный выбор действия администратором: {}", choice);
                    System.out.println("Неправильный выбор. Попробуйте снова.");
            }
        }
    }

    // Метод для запуска интерфейса пользователя
    private static void runUserInterface(long userId, OtpService otpService, String token) {

        logger.info("Интерфейс пользователя запущен для ID: {}", userId);

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
                                logger.info("Инициирование операции №{}, доставка OTP-кода по электронной почте", id_operation);
                                callInitiateOperationApi(id_operation, description, userId, "email", token);
                                break;
                            case 2:
                                logger.info("Инициирование операции №{}, доставка OTP-кода по SMS", id_operation);
                                callInitiateOperationApi(id_operation, description, userId, "sms", token);
                                break;
                            case 3:
                                logger.info("Инициирование операции №{}, доставка OTP-кода через Telegram", id_operation);
                                callInitiateOperationApi(id_operation, description, userId, "telegram", token);
                                break;
                            case 4:
                                logger.info("Инициирование операции №{}, сохранение OTP-кода в файле", id_operation);
                                callInitiateOperationApi(id_operation, description, userId, "file", token);
                                break;
                            case 0:
                                // Остановка планировщика задач
                                otpService.shutdown();

                                logger.info("Пользователь завершил сессию");
                                System.out.println("Выход.");
                                return;
                            default:
                                logger.warn("Некорректный выбор канала доставки OTP-кода: {}", choice_);
                                System.out.println("Неправильный выбор. Попробуйте снова.");
                        }
                    } else {
                        logger.warn("Неуспешная проверка OTP-кода: токен недействителен");
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 2:
                    if (checkTokenValidity(token)) {

                        //Получаем от пользователя OTP код
                        System.out.print("OTP-код: ");
                        String code = getInput("");

                        logger.info("Проверка введённого OTP-кода: {}", code);
                        callVerifyOtpCodeApi(code, token);
                    } else {
                        logger.warn("Ошибка проверки OTP-кода: токен недействителен");
                        System.out.println("Токен истек или недействителен. Повторите попытку.");
                    }
                    break;
                case 0:
                    // Остановка планировщика задач
                    otpService.shutdown();
                    logger.info("Пользователь завершил сессию");
                    System.out.println("Завершаем работу приложения.");
                    return;
                default:
                    logger.warn("Некорректный выбор действия пользователем: {}", choice);
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
        logger.debug("\nГенерация JWT-токена для пользователя: {}", username);
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
        logger.debug("\nПроверка действительного состояния токена: {}", token);
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
            logger.error("\nОшибка при проверке токена: " + e.getMessage());
            return false;
        }
    }

    // Вызов API для изменения конфигурации OTP
    private static void callChangeOtpConfigApi(String token) {
        logger.info("\nНачинается изменение конфигурации OTP");

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
                logger.info("\nКонфигурация OTP обновлена успешно.");
                System.out.println("Конфигурация OTP обновлена успешно.");
            } else {
                logger.error("\nОшибка обновления конфигурации OTP: " + conn.getResponseMessage());
                System.out.println("Ошибка обновления конфигурации OTP: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            logger.error("\nОшибка соединения с API: " + e.getMessage());
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }

    // Вызов API для получения списка пользователей
    private static void callListUsersApi(String token) {
        logger.info("\nНачинается получение списка пользователей");

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
                logger.info("\nСписок пользователей:\n" + response.toString());
                System.out.println("Список пользователей:\n" + response.toString());
            } else {
                logger.error("\nОшибка получения списка пользователей: " + conn.getResponseMessage());
                System.out.println("Ошибка получения списка пользователей: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            logger.error("\nОшибка соединения с API: " + e.getMessage());
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }

    // Вызов API для удаления пользователя (администратор)
    private static void callDeleteUserApi(String token) {
        logger.info("\nНачинается удаление пользователя");

        try {
            System.out.print("Введите id пользователя для удаления: ");
            long userId = Long.parseLong(getInput(""));

            URL url = new URL("http://localhost:8000/admin/delete-user/" + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("\nПользователь удален успешно.");
                System.out.println("Пользователь удален успешно.");
            } else {
                System.out.println("Ошибка удаления пользователя: " + conn.getResponseMessage());
                logger.error("\nОшибка удаления пользователя: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            logger.error("\nОшибка соединения с API: " + e.getMessage());
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }

    // Вызов API для удаления пользователя (пользователь)
    private static void callRegisterUserApi() {

        logger.info("\nРегистрация пользователя началась...");

        String username = getInput("Укажите имя: ");
        String password = getInput("Пароль: ");
        String role = getInput("Укажите свою роль (ADMIN/USER): ");

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
            json.put("role", role);

            // Отправляем данные
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();

            // Читаем ответ
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                logger.info("\nПользователь зарегистрирован успешно.");
                System.out.println("Пользователь зарегистрирован успешно.");
            } else {
                System.out.println("Ошибка регистрации: " + conn.getResponseMessage());
                logger.error("\nОшибка регистрации: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            logger.error("\nОшибка соединения с API: " + e.getMessage());
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }

    // Вызов API для входа пользователя (пользователь)
    private static JSONObject callLoginUserApi(String username, String password) {

        logger.info("\nНачинается процедура авторизации пользователя с именем '{}'", username);

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
                logger.error("\nОшибка входа: " + conn.getResponseMessage());
                System.out.println("Ошибка входа: " + conn.getResponseMessage())
                return null; // Возвращаем null, если ответ не OK
            }
        } catch (IOException e) {
            logger.error("\nОшибка соединения с API: " + e.getMessage());
            System.out.println("Ошибка соединения с API: " + e.getMessage());
            return null; // Возвращаем null, если возникла ошибка
        }
    }

    // Вызов API для генерации кода OTP (пользователь)
    private static void callInitiateOperationApi(long operationId, String description, long userId, String channel, String token) {
        logger.info("\nИнициируется операция с ID: {} и описанием: {}. Доставка: {}", operationId, description, channel);

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
                // Читаем тело ответа
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String output;
                while ((output = br.readLine()) != null) {
                    JSONObject responseJson = new JSONObject(output);

                    // Извлекаем номер OTP-кода и канал отправки из ответа
                    String otpCode = responseJson.optString("otp_code", "");
                    String sentChannel = responseJson.optString("sent_channel", "");

                    // Выводим информацию в консоль
                    System.out.println("Операция инициирована успешно.");
                    System.out.println("Код OTP: " + otpCode);
                    System.out.println("Канал отправки: " + channel);
                }
            } else {
                logger.error("\nОшибка инициирования операции: " + conn.getResponseMessage());
                System.out.println("Ошибка инициирования операции: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            logger.error("\nОшибка соединения с API: " + e.getMessage());
            System.out.println("Ошибка соединения с API: " + e.getMessage());
        }
    }

    // Вызов API для проверки OTP кода (пользователь)

    private static void callVerifyOtpCodeApi(String code, String token) {

        logger.info("\nПроизводится проверка OTP-кода: {}", code);

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
                logger.error("\nОшибка проверки OTP-кода: " + conn.getResponseMessage());
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с API: " + e.getMessage());
            logger.error("\nОшибка соединения с API: " + e.getMessage());
        }
    }
}

