package org.example.api;

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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Date;

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
        server.createContext("/login", new LoginHandler(token));
        server.createContext("/initiate-operation", new InitiateOperationHandler());
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

    // Handler for logging in an existing user
    class LoginHandler implements HttpHandler {

        private final String token;

        public LoginHandler(String token) {
            this.token = token;
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
                // Generate token
                JSONObject responseJson = new JSONObject();
                responseJson.put("token", token);
                sendSuccessResponse(exchange, responseJson.toString());
            } else {
                sendErrorResponse(exchange, 401, "Unauthorized: Invalid credentials.");
            }
        }
    }

    // Handler for initiating a protected operation
    class InitiateOperationHandler implements HttpHandler {
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

            // Create an operation object
            Operation operation = new Operation(operationId, description, userId);

            // Determine where to send the OTP code
            String destination = json.getString("destination");
            String channel = json.getString("channel"); // email, SMS, etc.

            // Generate OTP code
            int codeLength = otpService.getCodeLength();
            int lifeTimeInMinutes = otpService.getLifeTimeInMinutes();
            String generatedOtpCode = otpService.generateOtpCode(codeLength);
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime expirationTime = currentTime.plusMinutes(lifeTimeInMinutes);

            // Create OTP code object
            OtpCode otpCode = new OtpCode(
                    operation.getUserId(),
                    operation.getId(),
                    generatedOtpCode,
                    "ACTIVE",
                    currentTime,
                    expirationTime,
                    operation.getDescription());

            // Send OTP code based on the selected channel
            if (channel.equals("email")) {
                otpService.initiateOperationToEmail(destination, otpCode);
            } else if (channel.equals("sms")) {
                otpService.initiateOperationToSmpp(destination, otpCode);
            } else {
                sendErrorResponse(exchange, 400, "Unsupported channel.");
                return;
            }

            JSONObject responseJson = new JSONObject();
            responseJson.put("message", "OTP code sent successfully.");
            sendSuccessResponse(exchange, responseJson.toString());
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

    private String generateJwtToken(User user) {
        // Example of generating a JWT token
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        DefaultClaims claims = new DefaultClaims();
        claims.setSubject(user.getUsername()); // Subject (user identifier)
        claims.setIssuedAt(now);              // Issued at time
        claims.setExpiration(new Date(nowMillis + 3600000)); // Expiration time (1 hour)

        // Signature key for signing the token
        String secretKey = "mySecretKey"; // Replace with real secret key!

        // Build and sign the token
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
}