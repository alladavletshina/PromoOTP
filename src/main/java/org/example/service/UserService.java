package org.example.service;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.List;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllUsers() {
        return userDao.findAllUsers();
    }

    public boolean isExistAdmin() {
        List<User> admins = userDao.findAllAdmins();
        return !admins.isEmpty(); // Вернём true, если хотя бы один администратор есть
    }

    public User getUserByUsername(String username) {
        return userDao.findUserByUsername(username);
    }

    public boolean addUser(User user) {
        return userDao.saveUser(user);
    }

    public boolean updateUser(User user) {
        return userDao.updateUser(user);
    }

    public boolean deleteUser(long id) {
        return userDao.deleteUser(id);
    }

    // Новый метод для получения роли пользователя
    public String getRole(String username) {
        return userDao.getRole(username);
    }
}