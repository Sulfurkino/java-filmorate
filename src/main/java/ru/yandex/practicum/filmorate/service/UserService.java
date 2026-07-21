package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage UserStorage;

    public UserService(UserStorage userStorage) {
        this.UserStorage = userStorage;
    }

    public User create(User user) {
        return UserStorage.create(user);
    }

    public User update(User user){
        return UserStorage.update(user);
    }

    public List<User> getAll(){
        return UserStorage.getAll();
    }

    public void validateUserId(Long userId) {

    }
}
