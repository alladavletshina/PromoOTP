package org.example.service;

import org.example.model.Operation;
import org.example.util.EmailNotificationService;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;

import java.time.LocalDateTime;
import java.util.Random;

public class OtpService {

    private final EmailNotificationService emailService;
    private final SmppClient smsSender;
    private final TelegramBot telegramBot;

    // Конфигурационные параметры для OTP-кодов
    private int otpCodeLength = 6; // Длина OTP-кода
    private int otpLifetimeInMinutes = 10; // Время жизни OTP-кода в минутах

    public OtpService(EmailNotificationService emailService, SmppClient smsSender, TelegramBot telegramBot) {
        this.emailService = emailService;
        this.smsSender = smsSender;
        this.telegramBot = telegramBot;
    }

    public void initiateOperation(Operation operation) {
        // Генерируем уникальный OTP-код для этой операции
        String otpCode = generateOtpCode();
        // Сохраняем OTP-код в базу данных (здесь заглушка)
        System.out.println("Сохранён OTP-код для операции: " + operation.getDescription());
        // Отправляем OTP-код пользователю через выбранный канал
        sendOtpCodeToUser(operation.getUserEmail(), otpCode);
    }

    public void completeOperation(Long operationId) {
        // Проверяем статус операции и завершаем её (здесь заглушка)
        System.out.println("Операция с ID " + operationId + " завершена.");
    }

    public String generateOtpCodeForUser(String userEmail) {
        // Генерируем уникальный OTP-код
        String otpCode = generateOtpCode();
        // Отправляем OTP-код пользователю через выбранный канал
        sendOtpCodeToUser(userEmail, otpCode);
        return otpCode; // Возвращаем сгенерированный OTP-код
    }

    private String generateOtpCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpCodeLength; i++) {
            sb.append(random.nextInt(10)); // Генерируем случайную цифру
        }
        return sb.toString();
    }

    // Изменяем видимость метода на публичную
    public void sendOtpCodeToUser(String userEmail, String otpCode) {
        // Выбор канала отправки зависит от предпочтений пользователя
        // Здесь мы предполагаем, что используется Email
        //emailService.sendEmail(userEmail, "Ваш защитный код", "Ваш защитный код: " + otpCode);
        System.out.println("Защитный код отправлен на почту: " + userEmail);
    }

    public boolean checkOtpCode(String userEmail, String otpCode) {
        // Проверяем OTP-код, сравнивая с сохранённым (здесь заглушка)
        System.out.println("Проверка OTP-кода для пользователя: " + userEmail);
        // Для теста считаем, что код верный
        return true;
    }

    public void changeOtpConfig(int newCodeLength, int newLifetimeInMinutes) {
        this.otpCodeLength = newCodeLength;
        this.otpLifetimeInMinutes = newLifetimeInMinutes;
        System.out.println("Новая конфигурация OTP-кодов установлена: длина кода " + newCodeLength + ", время жизни " + newLifetimeInMinutes + " минут.");
    }
}