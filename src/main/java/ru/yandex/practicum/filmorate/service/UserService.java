package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

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
        User user = validateUserId(id);
        User friend = validateUserId(friendId);

        boolean result = user.getFriends().add(friendId);
        friend.getFriends().add(id);

        log.info("Пользователи {} и {} стали друзьями", id, friendId);

        return result;
    }

    public boolean removeFriend(Long id, Long friendId) {
        User user = validateUserId(id);
        User friend = validateUserId(friendId);

        boolean result = user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        log.info("Пользователи {} и {} больше не друзья", id, friendId);

        return result;
    }

    public List<User> getFriends(Long id) {
        User user = validateUserId(id);

        return user.getFriends().stream()
                .map(this::validateUserId)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        User first = validateUserId(id);
        User second = validateUserId(otherId);

        return first.getFriends().stream()
                .filter(second.getFriends()::contains)
                .map(this::validateUserId)
                .collect(Collectors.toList());
    }
}