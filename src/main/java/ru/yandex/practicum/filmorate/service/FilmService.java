package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilmId(film.getId());
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film validateFilmId(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Фильм с id=" + id + " не найден"));
    }

    public boolean addLike(Long filmId, Long userId) {
        Film film = validateFilmId(filmId);
        validateUserId(userId);

        boolean result = film.getLikes().add(userId);

        filmStorage.update(film);

        return result;
    }

    public boolean removeLike(Long filmId, Long userId) {
        Film film = validateFilmId(filmId);
        validateUserId(userId);

        boolean result = film.getLikes().remove(userId);

        filmStorage.update(film);

        return result;
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(
                                (Film film) -> film.getLikes().size())
                        .reversed())
                .limit(count)
                .toList();
    }

    private User validateUserId(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь с id=" + id + " не найден"));
    }
}