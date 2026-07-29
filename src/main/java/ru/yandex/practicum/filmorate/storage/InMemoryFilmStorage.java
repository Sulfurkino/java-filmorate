package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;


    @Override
    public Optional<Film> findById(Long id) {
        if (films.containsKey(id)) {
            return Optional.of(films.get(id));
        }
        return Optional.empty();
    }


    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {

        films.put(film.getId(), film);

        return film;
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        return films.get(filmId).getLikes().add(userId);
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        return films.get(filmId).getLikes().remove(userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed()
                        .thenComparing(Film::getId))
                .limit(count)
                .toList();
    }
}
