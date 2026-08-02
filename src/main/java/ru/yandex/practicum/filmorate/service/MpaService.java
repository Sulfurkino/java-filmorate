package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> getAll() {
        return mpaStorage.getAll();
    }

    public Mpa getById(Long id) {
        return mpaStorage.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Рейтинг MPA с id=" + id + " не найден"));
    }
}
