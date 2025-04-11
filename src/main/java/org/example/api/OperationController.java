package org.example.api;

import org.example.model.OtpCode;
import org.example.service.OtpService;
import org.example.model.Operation;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.example.Main.getInput;

public class OperationController {

    private final OtpService otpService;

    public OperationController(OtpService otpService) {
        this.otpService = otpService;
    }

    public void initiateProtectedOperationToSmpp(Operation operation) {

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

    public void initiateProtectedOperationToEmail(Operation operation) {

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
    public void saveOtpCodeToFile(Operation operation) {
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

    public void verifyOtpCode(String userEmail, String otpCode) {
        System.out.println("Проверка OTP-кода для пользователя " + userEmail + ". Код: " + otpCode);

        boolean isValid = otpService.checkOtpCode(userEmail, otpCode);
        if (isValid) {
            System.out.println("OTP-код верен. Операция разрешена.");
        } else {
            System.out.println("OTP-код неверен. Операция отклонена.");
        }
    }
}