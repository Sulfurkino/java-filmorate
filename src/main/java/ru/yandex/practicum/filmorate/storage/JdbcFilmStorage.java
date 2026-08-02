package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcFilmStorage implements FilmStorage {
    private static final FilmRowMapper FILM_ROW_MAPPER = new FilmRowMapper();
    private static final GenreRowMapper GENRE_ROW_MAPPER = new GenreRowMapper();
    private static final String SELECT_FILMS = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   m.id AS mpa_id, m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON m.id = f.mpa_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = jdbcTemplate.query(SELECT_FILMS + " ORDER BY f.id", FILM_ROW_MAPPER);
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        List<Film> films = jdbcTemplate.query(SELECT_FILMS + " WHERE f.id = ?", FILM_ROW_MAPPER, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.getFirst();
        loadRelations(film);
        return Optional.of(film);
    }

    @Override
    @Transactional
    public Film create(Film film) {
        String sql = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setObject(3, film.getReleaseDate());
            statement.setInt(4, film.getDuration());
            statement.setLong(5, film.getMpa().getId());
            return statement;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        replaceGenres(film);
        return findById(film.getId()).orElseThrow();
    }

    @Override
    @Transactional
    public Film update(Film film) {
        jdbcTemplate.update("""
                        UPDATE films
                        SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                        WHERE id = ?
                        """,
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId(), film.getId());
        replaceGenres(film);
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        return jdbcTemplate.update("""
                MERGE INTO film_likes (film_id, user_id) KEY(film_id, user_id)
                VALUES (?, ?)
                """, filmId, userId) > 0;
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        return jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId, userId) > 0;
    }

    @Override
    public List<Film> getPopular(int count) {
        List<Long> ids = jdbcTemplate.queryForList("""
                SELECT f.id
                FROM films f
                LEFT JOIN film_likes fl ON fl.film_id = f.id
                GROUP BY f.id
                ORDER BY COUNT(fl.user_id) DESC, f.id ASC
                LIMIT ?
                """, Long.class, count);
        return ids.stream().map(id -> findById(id).orElseThrow()).toList();
    }

    private void loadRelations(Film film) {
        film.getGenres().addAll(jdbcTemplate.query("""
                SELECT g.id, g.name
                FROM genres g
                JOIN films_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """, GENRE_ROW_MAPPER, film.getId()));
        film.getLikes().addAll(jdbcTemplate.queryForList(
                "SELECT user_id FROM film_likes WHERE film_id = ? ORDER BY user_id",
                Long.class, film.getId()));
    }

    private void replaceGenres(Film film) {
        jdbcTemplate.update("DELETE FROM films_genres WHERE film_id = ?", film.getId());
        film.getGenres().stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(genreId -> jdbcTemplate.update(
                        "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genreId));
    }
}
