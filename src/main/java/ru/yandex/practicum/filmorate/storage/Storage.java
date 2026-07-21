package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

public interface Storage <T>{
    T create(T element);
    T update(T element);
    List<T> getAll();
    Optional<T> findById(Long id);
}
