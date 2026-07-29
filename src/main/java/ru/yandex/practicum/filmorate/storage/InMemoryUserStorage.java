package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public Optional<User> findById(Long id) {
        if (users.containsKey(id)) {
            return Optional.of(users.get(id));
        }
        return Optional.empty();
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new EntityNotFoundException(
                    "Пользователь с id=" + user.getId() + " не найден");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);

        log.info("Обновлен пользователь с id={}", user.getId());

        return user;
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(nextId++);
        users.put(user.getId(), user);

        log.info("Добавлен пользователь с id={}", user.getId());

        return user;
    }

    @Override
    public boolean addFriend(Long id, Long friendId) {
        return users.get(id).getFriends().add(friendId);
    }

    @Override
    public boolean removeFriend(Long id, Long friendId) {
        return users.get(id).getFriends().remove(friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        return users.get(id).getFriends().stream().map(users::get).toList();
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        return users.get(id).getFriends().stream()
                .filter(users.get(otherId).getFriends()::contains)
                .map(users::get)
                .toList();
    }
}
