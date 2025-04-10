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

    public User getUserByUsername(String username, String password) {
        return userDao.findUserByUsername(username, password);
    }

    public boolean addUser(User user) {
        // Можно добавить проверку валидности пользователя перед сохранением
        return userDao.saveUser(user);
    }

    public boolean updateUser(User user) {
        // Можно добавить проверку валидности пользователя перед обновлением
        return userDao.updateUser(user);
    }

    public boolean deleteUser(long id) {
        return userDao.deleteUser(id);
    }

    // Новый метод для получения роли пользователя
    public User.Role getRole(String username) {
        return userDao.getRole(username);
    }
}