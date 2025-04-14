package org.example.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.service.OtpService;
import org.example.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.example.model.User;
import org.example.dao.UserDao;
import org.example.dao.OtpDao;
import org.example.util.EmailNotificationService;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;

public class AdminApi {

    private static final int PORT = 8000;

    public static void main(String[] args) throws IOException {
        // Создание экземпляров DAO и сервисов
        UserDao userDao = new UserDao();
        OtpDao otpDao = new OtpDao();
        EmailNotificationService emailService = new EmailNotificationService();
        SmppClient smsSender = new SmppClient();
        TelegramBot telegramBot = new TelegramBot();

        UserService userService = new UserService(userDao);
        OtpService otpService = new OtpService(emailService, otpDao, smsSender, telegramBot);

        // Создание и настройка HTTP-сервера
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/admin/configure-otp", new ConfigureOtpHandler(otpService));
        server.createContext("/admin/list-users", new ListUsersHandler(userService));
        server.createContext("/admin/delete-user", new DeleteUserHandler(userService));
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Admin API server started on port " + PORT);
    }


    static class ConfigureOtpHandler implements HttpHandler {
        private OtpService otpService;

        public ConfigureOtpHandler(OtpService otpService) {
            this.otpService = otpService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }


            String requestBody = readRequestBody(exchange);
            String[] params = requestBody.split("&");
            List<String> paramList = new ArrayList<>();
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    paramList.add(keyValue[1]);
                }
            }

            if (paramList.size() >= 2) {
                int codeLength = Integer.parseInt(paramList.get(0));
                int lifetimeInMinutes = Integer.parseInt(paramList.get(1));


                try {
                    otpService.changeOtpConfig(codeLength, lifetimeInMinutes);
                    sendSuccessResponse(exchange, "OTP configuration updated successfully.");
                } catch (Exception e) {
                    sendErrorResponse(exchange, 500, "Failed to update OTP configuration: " + e.getMessage());
                }
            } else {
                sendErrorResponse(exchange, 400, "Missing or invalid parameters.");
            }
        }
    }


    static class ListUsersHandler implements HttpHandler {
        private final UserService userService;

        public ListUsersHandler(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            try {
                List<User> users = userService.getAllUsers();
                String response = "";
                for (User user : users) {
                    response += user.getId() + " " + user.getUsername() + " (" + user.getRole() + ")\n";
                }
                sendSuccessResponse(exchange, response);
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Failed to retrieve users: " + e.getMessage());
            }
        }
    }


    static class DeleteUserHandler implements HttpHandler {

        private final UserService userService;

        public DeleteUserHandler(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("DELETE")) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }


            String path = exchange.getRequestURI().toString();
            String[] pathParts = path.split("/");
            if (pathParts.length < 3) {
                sendErrorResponse(exchange, 404, "User ID not found in the request.");
                return;
            }
            long userId = Long.parseLong(pathParts[pathParts.length - 1]);

            // Call service method to delete the user
            try {
                userService.deleteUser(userId);
                sendSuccessResponse(exchange, "User deleted successfully.");
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Failed to delete user: " + e.getMessage());
            }
        }
    }


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