package org.example.api;

import org.example.model.User;
import org.example.service.OtpService;
import org.example.service.UserService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import static org.example.Main.getInput;

import java.util.Date;

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

}