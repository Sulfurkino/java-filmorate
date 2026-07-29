package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        validateUserId(user.getId());
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User validateUserId(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public boolean addFriend(Long id, Long friendId) {
        validateUserId(id);
        validateUserId(friendId);
        boolean result = userStorage.addFriend(id, friendId);
        log.info("Пользователь {} добавил пользователя {} в друзья", id, friendId);
        return result;
    }

    public boolean removeFriend(Long id, Long friendId) {
        validateUserId(id);
        validateUserId(friendId);
        boolean result = userStorage.removeFriend(id, friendId);
        log.info("Пользователь {} удалил пользователя {} из друзей", id, friendId);
        return result;
    }

    public List<User> getFriends(Long id) {
        validateUserId(id);
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        validateUserId(id);
        validateUserId(otherId);
        return userStorage.getCommonFriends(id, otherId);
    }
}