package org.example.util;

public class EmailNotificationService {

    public void sendEmail(String recipient, String subject, String message) {
        System.out.println("Отправлено письмо на адрес " + recipient + " с темой '" + subject + "' и содержанием:\n" + message);
    }
}
