package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage extends Storage<Film> {
    boolean addLike(Long filmId, Long userId);

    boolean removeLike(Long filmId, Long userId);

    List<Film> getPopular(int count);
}
