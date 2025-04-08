package org.example.util;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailNotificationService {

    private String username;
    private String password;
    private String fromEmail;
    private Session session;

    public EmailNotificationService() {
        // Загружаем конфигурацию из файла email.properties
        Properties config = loadConfig();

        // Установка параметров
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");

        // Создание сессии для отправки писем
        this.session = Session.getInstance(config, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    // Метод для загрузки конфигурации из файла email.properties
    private Properties loadConfig() {
        try {
            Properties props = new Properties();
            InputStream inputStream = EmailNotificationService.class.getClassLoader()
                    .getResourceAsStream("email.properties");
            props.load(inputStream);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }

    // Метод для отправки письма с кодом подтверждения
    public void sendCode(String toEmail, String code) {
        try {
            // Создание нового сообщения
            Message message = new MimeMessage(session);

            // Установление отправителя и получателя
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // Тема и содержание письма
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);

            // Отправка письма
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public String getUserEmail() {
        return "example@email.com";
    }
}
