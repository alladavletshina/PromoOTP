package org.example.service;

import org.example.dao.OperationDao;
import org.example.dao.OtpDao;
import org.example.dao.UserDao;
import org.example.model.Operation;
import org.example.model.OtpCode;
import org.example.util.EmailNotificationService;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;

import java.time.LocalDateTime;
import java.util.Random;


public class OtpService {

    private final EmailNotificationService emailService;
    private final OtpDao otpDao;
    //private final SmppClient smsSender;
    //private final TelegramBot telegramBot;

    // Конфигурационные параметры для OTP-кодов
    private int otpCodeLength = 6; // Длина OTP-кода
    private int otpLifetimeInMinutes = 10; // Время жизни OTP-кода в минутах

    public OtpService(EmailNotificationService emailService, OtpDao otpDao) {
        this.emailService = emailService;
        this.otpDao = otpDao;
    }

    public void initiateOperationToEmail(String toEmail, OtpCode otpCode) {
        //emailService.sendCode(toEmail, otpCode.getCode());
        otpDao.saveOtpCode(otpCode);
    }

    public int getCodeLength() {
        return otpDao.getCodeLength();
    }

    public int getLifeTimeInMinutes() {
        return otpDao.getLifeTimeInMinutes();
    }

    public void saveOtpCodeToFile(OtpCode otpCode) {
        otpDao.saveOtpCode(otpCode);
    }

    public String generateOtpCode(int otpCodeLength) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpCodeLength; i++) {
            sb.append(random.nextInt(10)); // Генерируем случайную цифру
        }
        return sb.toString();
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
        otpDao.changeOtpConfig(newCodeLength, newLifetimeInMinutes);
    }
}