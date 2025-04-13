package org.example.service;

import org.example.dao.OtpDao;
import org.example.model.Operation;
import org.example.model.OtpCode;
import org.example.util.EmailNotificationService;
import org.example.util.SmppClient;
import org.example.util.TelegramBot;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;


public class OtpService {

    private final EmailNotificationService emailService;
    private final OtpDao otpDao;
    private final SmppClient smsSender;
    private ScheduledExecutorService scheduler;
    //private final TelegramBot telegramBot;

    public OtpService(EmailNotificationService emailService, OtpDao otpDao, SmppClient smsSender) {
        this.emailService = emailService;
        this.otpDao = otpDao;
        this.smsSender = smsSender;
    }

    public void initiateOperationToEmail(OtpCode otpCode) {
        String destination = "atdavletshina@gmail.com";
        emailService.sendCode(destination,otpCode.getCode());
        otpDao.saveOtpCode(otpCode);
    }

    public void initiateOperationToSmpp(OtpCode otpCode) {
        smsSender.loadProperties();
        String destination = "+7";
        smsSender.sendSms(destination, otpCode.getCode());
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

    public List<OtpCode> findAllOtpCodes() {
        return otpDao.findAllOtpCodes();
    }

    public void processExpiredOtpCodes(List<OtpCode> otpCodes) {
        LocalDateTime currentTime = LocalDateTime.now();

        for (OtpCode otpCode : otpCodes) {
            if (currentTime.isAfter(otpCode.getExpirationTime())) {
                otpCode.setStatus("EXPIRED");
                otpDao.updateOtpCodeStatus(otpCode); // Обновляем статус в базе данных
            }
        }
    }

    public void initScheduler() {
        int intervalInSeconds = 5000;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            List<OtpCode> otpCodes = otpDao.findAllOtpCodes(); // Загружаем все OTP-коды из базы данных
            processExpiredOtpCodes(otpCodes); // Обрабатываем просроченные OTP-коды
        }, 0, intervalInSeconds, TimeUnit.SECONDS);
    }



    public void shutdown() {
        scheduler.shutdownNow();
    }

    public boolean getOtpCode(String otpCode) {
      return otpDao.getOtpCode(otpCode);
    }

    public void changeOtpConfig(int newCodeLength, int newLifetimeInMinutes) {
        System.out.println("Новая конфигурация OTP-кодов установлена: длина кода " + newCodeLength + ", время жизни " + newLifetimeInMinutes + " минут.");
        otpDao.changeOtpConfig(newCodeLength, newLifetimeInMinutes);
    }
}