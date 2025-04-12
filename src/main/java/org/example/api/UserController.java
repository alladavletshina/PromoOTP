package org.example.api;

import org.example.model.Operation;
import org.example.model.OtpCode;
import org.example.model.User;
import org.example.service.OtpService;
import org.example.service.UserService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.json.JSONObject;

import static org.example.Main.getInput;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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

        User user = new User(username, password, User.Role.USER);

        try {
            if (userService.addUser(user)) {
                System.out.println("Пользователь зарегистрирован успешно.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
        }
    }

    public String getUserToken(String name, String password) {
        try {
            // Проверяем существование пользователя
            User user = userService.getUserByUsername(name, password);
            if (user != null) {
                // Генерируем токен
                String token = generateJwtToken(user);
                return token; // Возвращаем токен
            } else {
                return null; // Возвращаем null, если пользователь не найден
            }
        } catch (Exception e) {
            System.out.println("Ошибка входа: " + e.getMessage());
            return null; // Возвращаем null в случае ошибки
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

    private String generateJwtToken(User user) {
        // Пример генерации JWT-токена
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(user.getUsername()); // Установим субъект (идентификатор пользователя)
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

    public void initiateProtectedOperationToSmpp(Operation operation, String token) {

        //данные для генерации OTP кода
        int CodeLength = otpService.getCodeLength();
        int lifeTimeInMinutes = otpService.getLifeTimeInMinutes();

        String generatedOtpCode = otpService.generateOtpCode(CodeLength);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expirationTime = currentTime.plusMinutes(lifeTimeInMinutes);

        //создан объект OTP кода
        OtpCode otpCode = new OtpCode(
                operation.getUserId(),
                operation.getId(),
                generatedOtpCode,
                "ACTIVE",
                currentTime,
                expirationTime,
                operation.getDescription());

        System.out.println("\nСгенерирован OTP-код: " + otpCode.getCode());

        System.out.println("\nИнициирована защищённая операция: " + operation.getDescription());

        String destination = " +79150887621.";

        try {
            otpService.initiateOperationToSmpp(destination, otpCode);
            System.out.println("OTP-код отправлен на ваш номер " + destination);
        } catch (Exception e) {
            System.out.println("Ошибка генерации OTP-кода: " + e.getMessage());
        }
    }

    public void initiateProtectedOperationToEmail(Operation operation, String token) {

        //данные для генерации OTP кода
        int CodeLength = otpService.getCodeLength();
        int lifeTimeInMinutes = otpService.getLifeTimeInMinutes();

        String generatedOtpCode = otpService.generateOtpCode(CodeLength);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expirationTime = currentTime.plusMinutes(lifeTimeInMinutes);

        //создан объект OTP кода
        OtpCode otpCode = new OtpCode(
                operation.getUserId(),
                operation.getId(),
                generatedOtpCode,
                "ACTIVE",
                currentTime,
                expirationTime,
                operation.getDescription());

        System.out.println("\nСгенерирован OTP-код: " + otpCode.getCode());

        System.out.print("\nЭлектронная почта: ");
        String email = getInput("");

        System.out.println("\nИнициирована защищённая операция: " + operation.getDescription());

        try {
            otpService.initiateOperationToEmail(email, otpCode);
            System.out.println("OTP-код отправлен на вашу электронную почту.");
        } catch (Exception e) {
            System.out.println("Ошибка генерации OTP-кода: " + e.getMessage());
        }
    }

    // Метод для сохранения всех данных OTP-кода в файл в текущей директории
    public void saveOtpCodeToFile(Operation operation, String token) {
        // Данные для генерации OTP-кода
        int CodeLength = otpService.getCodeLength();
        int lifeTimeInMinutes = otpService.getLifeTimeInMinutes();

        String generatedOtpCode = otpService.generateOtpCode(CodeLength);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expirationTime = currentTime.plusMinutes(lifeTimeInMinutes);

        //создан объект OTP кода
        OtpCode otpCode = new OtpCode(
                operation.getUserId(),
                operation.getId(),
                generatedOtpCode,
                "ACTIVE",
                currentTime,
                expirationTime,
                operation.getDescription());

        System.out.println("\nСгенерирован OTP-код: " + otpCode.getCode());

        System.out.println("\nИнициирована защищённая операция: " + operation.getDescription());

        otpService.saveOtpCodeToFile(otpCode);

        // Указываем относительный путь к файлу в текущей директории
        String fileName = "otp_code.txt";
        String filePath = System.getProperty("user.dir") + "/" + fileName;

        // Преобразуем объект OtpCode в JSON
        JSONObject jsonObject = new JSONObject(otpCode);
        String jsonData = jsonObject.toString();

        // Открываем файл для записи и перезаписи информации
        try (FileWriter writer = new FileWriter(filePath, false)) {
            writer.write(jsonData);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Информируем пользователя о сохранении данных
        System.out.println("\nВсе данные OTP-кода успешно сохранены в файл: " + filePath);
    }

    public void verifyOtpCode(String token) {
        System.out.print("OTP-код: ");
        String code = getInput("");

        try {
            boolean isValid = otpService.getOtpCode(code);
            if (isValid) {
                System.out.println("Код введен верно!");
            } else {
                System.out.println("Неверный код!");
            }
        } catch (Exception e) {
            System.out.println("Ошибка проверки OTP-кода: " + e.getMessage());
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