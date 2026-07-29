package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Film create(Film film) {
        validateReferences(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilmId(film.getId());
        validateReferences(film);
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
        validateFilmId(filmId);
        validateUserId(userId);
        return filmStorage.addLike(filmId, userId);
    }

    public boolean removeLike(Long filmId, Long userId) {
        validateFilmId(filmId);
        validateUserId(userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    private void validateUserId(Long id) {
        userStorage.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void validateReferences(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }
        Long mpaId = film.getMpa() == null ? null : film.getMpa().getId();
        mpaStorage.findById(mpaId).orElseThrow(() ->
                new EntityNotFoundException("Рейтинг MPA с id=" + mpaId + " не найден"));
        film.getGenres().forEach(genre -> {
            Long genreId = genre == null ? null : genre.getId();
            genreStorage.findById(genreId).orElseThrow(() ->
                    new EntityNotFoundException("Жанр с id=" + genreId + " не найден"));
        });
    }
}