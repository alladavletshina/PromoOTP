package org.example.model;

import java.time.LocalDateTime;

import org.example.util.EmailNotificationService;
import java.time.LocalDateTime;

public class Operation {

    private Long id;
    private String description;
    private Long userId;
    private LocalDateTime startTime; // Время начала операции
    private LocalDateTime endTime;   // Время завершения операции



    // Конструктор с параметрами
    public Operation(Long id, String description, Long userId) {
        this.id = id;
        this.description = description;
        this.userId = userId;
    }

    // Конструктор с параметрами
    public Operation(String description, Long userId) {
        this.description = description;
        this.userId = userId;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    // Методы для управления операцией
    public void startOperation() {
        this.startTime = LocalDateTime.now();
        System.out.println("Операция началась: " + this.description);
    }

    public void completeOperation() {
        this.endTime = LocalDateTime.now();
        System.out.println("Операция завершена: " + this.description);
    }

    // Добавляем метод getUserEmail() для получения email через MailNotificationService
    public String getUserEmail() {
        return "atdavletshina@gmail.com";
    }

    // Переопределенный метод toString для удобного отображения объекта
    @Override
    public String toString() {
        return "Operation{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", userId=" + userId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}