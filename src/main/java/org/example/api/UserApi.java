package org.example.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.example.dao.OtpDao;
import org.example.dao.UserDao;
import org.example.model.Operation;
import org.example.model.OtpCode;
import org.example.model.User;
import org.example.service.OtpService;
import org.example.service.UserService;

import java.io.*;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Date;
import org.json.JSONObject;

import org.example.util.EmailNotificationService;
import org.example.util.SmppClient;
import org.json.JSONObject;

public class UserApi {

    private static final int PORT = 9000;
    private static final String SECRET_KEY = "mySecretKey";

    private final UserService userService;
    private final OtpService otpService;
    private String token;

    public UserApi(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler(userService));
        server.createContext("/initiate-operation", new InitiateOperationHandler(otpService));
        server.createContext("/verify-otp-code", new VerifyOtpCodeHandler(otpService));

        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("User API server started on port " + PORT);
    }

    public static void main(String[] args) throws IOException {
        // Initialize services
        UserDao userDao = new UserDao();
        OtpDao otpDao = new OtpDao();
        EmailNotificationService emailService = new EmailNotificationService();
        SmppClient smsSender = new SmppClient();
        UserService userService = new UserService(userDao);
        OtpService otpService = new OtpService(emailService, otpDao, smsSender);

        // Start the API server
        UserApi userApi = new UserApi(userService, otpService);
        userApi.startServer();
    }

    static class VerifyOtpCodeHandler implements HttpHandler {
        private final OtpService otpService;

        public VerifyOtpCodeHandler(OtpService otpService) {
            this.otpService = otpService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            // Read request body
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String code = json.getString("code");

            try {
                boolean isValid = otpService.getOtpCode(code);
                JSONObject responseJson = new JSONObject();
                if (isValid) {
                    responseJson.put("message", "Код введен верно!");
                } else {
                    responseJson.put("message", "Неверный код!");
                }
                sendSuccessResponse(exchange, responseJson.toString());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Ошибка проверки OTP-кода: " + e.getMessage());
            }
        }
    }

    // Handler for registering a new user
    class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            // Read request body
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String username = json.getString("username");
            String password = json.getString("password");

            // Create a new user
            User user = new User(username, password, User.Role.USER);

            try {
                if (userService.addUser(user)) {
                    sendSuccessResponse(exchange, "User registered successfully.");
                } else {
                    sendErrorResponse(exchange, 409, "Conflict: User already exists.");
                }
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    static class LoginHandler implements HttpHandler {

        private final UserService userService;

        public LoginHandler(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            // Read request body
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            String username = json.getString("username");
            String password = json.getString("password");

            // Check if user exists
            User user = userService.getUserByUsername(username, password);
            if (user != null) {
                // Prepare success response with both token and user data
                JSONObject responseJson = new JSONObject();
                responseJson.put("user", user.toJSONObject()); // Convert User object to JSON

                sendSuccessResponse(exchange, responseJson.toString());
            } else {
                sendErrorResponse(exchange, 401, "Unauthorized: Invalid credentials.");
            }
        }
    }

    // Handler for initiating a protected operation
    static class InitiateOperationHandler implements HttpHandler {

        private final OtpService otpService;

        public InitiateOperationHandler(OtpService otpService) {
            this.otpService = otpService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            // Read request body
            String requestBody = readRequestBody(exchange);
            JSONObject json = new JSONObject(requestBody);
            long operationId = json.getLong("id");
            String description = json.getString("description");
            long userId = json.getLong("user_id");

            // Determine where to send the OTP code
            String channel = json.getString("channel"); // email, SMS, etc.

            // Generate OTP code
            int codeLength = otpService.getCodeLength();
            int lifeTimeInMinutes = otpService.getLifeTimeInMinutes();
            String generatedOtpCode = otpService.generateOtpCode(codeLength);
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime expirationTime = currentTime.plusMinutes(lifeTimeInMinutes);

            // Create OTP code object
            OtpCode otpCode = new OtpCode(
                    userId,
                    operationId,
                    generatedOtpCode,
                    "ACTIVE",
                    currentTime,
                    expirationTime,
                    description);

            // Send OTP code based on the selected channel
            if (channel.equals("email")) {
                otpService.initiateOperationToEmail(otpCode);
            } else if (channel.equals("sms")) {
                otpService.initiateOperationToSmpp(otpCode);
            } else if (channel.equals("file")) {
                otpService.saveOtpCodeToFile(otpCode);
            } else {
                sendErrorResponse(exchange, 400, "Unsupported channel.");
                return;
            }

            // Создаем JSON-ответ с информацией о коде и канале отправки
            JSONObject responseJson = new JSONObject();
            responseJson.put("otp_code", generatedOtpCode);
            responseJson.put("sent_channel", channel);
            responseJson.put("message", "OTP code sent successfully.");

            // Отправляем успешный ответ
            sendSuccessResponse(exchange, responseJson.toString());
        }

        private static String readRequestBody(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        private void sendSuccessResponse(HttpExchange exchange, String responseBody) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
        }
    }

    // Helper methods
    private static void sendSuccessResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.sendResponseHeaders(statusCode, message.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        java.io.InputStreamReader isr = new java.io.InputStreamReader(exchange.getRequestBody());
        java.io.BufferedReader br = new java.io.BufferedReader(isr);
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }
        return requestBody.toString();
    }

}