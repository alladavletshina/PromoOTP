package org.example.model;

import java.time.LocalDateTime;

public class OtpCode {

    private long id;
    private String code;
    private OtpStatus status;
    private LocalDateTime creationTime;
    private LocalDateTime expirationTime;

    // Перечисление для статусов OTP-кода
    public enum OtpStatus {
        ACTIVE, EXPIRED, USED
    }

    // Конструкторы
    public OtpCode() {} // Необходимый пустой конструктор для JDBC

    public OtpCode(long id, String code, OtpStatus status, LocalDateTime creationTime, LocalDateTime expirationTime) {
        this.id = id;
        this.code = code;
        this.status = status;
        this.creationTime = creationTime;
        this.expirationTime = expirationTime;
    }

    // Геттеры и сеттеры
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    // Методы для проверки состояния OTP-кода
    public boolean isActive() {
        return status == OtpStatus.ACTIVE;
    }

    public boolean isExpired() {
        return status == OtpStatus.EXPIRED || LocalDateTime.now().isAfter(expirationTime);
    }

    public boolean isUsed() {
        return status == OtpStatus.USED;
    }

    // Переопределённый метод toString для удобного вывода
    @Override
    public String toString() {
        return "OtpCode{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", creationTime=" + creationTime +
                ", expirationTime=" + expirationTime +
                '}';
    }
}