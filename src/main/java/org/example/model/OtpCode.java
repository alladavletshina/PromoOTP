package org.example.model;

import java.time.LocalDateTime;

public class OtpCode {

    private long id;
    private String code;
    private String status;
    private LocalDateTime creationTime;
    private LocalDateTime expirationTime;

    // Конструкторы
    public OtpCode() {} // Необходимый пустой конструктор для JDBC

    public OtpCode(long id, String code, String status, LocalDateTime creationTime, LocalDateTime expirationTime) {
        this.id = id;
        this.code = code;
        this.status = status;
        this.creationTime = creationTime;
        this.expirationTime = expirationTime;
    }

    public OtpCode(String code, String status, LocalDateTime creationTime, LocalDateTime expirationTime) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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