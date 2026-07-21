package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.el.stream.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film create(Film film) {
        Film newFilm = filmStorage.create(film);
        log.info("Добавлен фильм с id={}", newFilm.getId());
        return newFilm;
    }

    public Film validateFilmId(Long id){
        return filmStorage.findById(id).orElseThrow(() -> {
            log.error("Фильм с таким id - " + id + " не найден.");
            throw new EntityNotFoundException(
                    "Фильм с id=" + id + " не найден"
            );
        });
    }

    public Film update(Film film){
        validateFilmId(film.getId());

        Film updateFilm = filmStorage.update(film);
        log.info("Обновлен фильм с id={}", film.getId());

        return updateFilm;
    }

    public List<Film> getAll(){
        return filmStorage.getAll();
    }

    public boolean addLike(Long filmId, Long userId) {
        Film film =  validateFilmId(filmId);
        userService.validateUserId(userId);
       return film.getLikes().add(userId);
    }



}
