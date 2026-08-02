package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcGenreStorage implements GenreStorage {
    private static final GenreRowMapper GENRE_ROW_MAPPER = new GenreRowMapper();
    private final JdbcTemplate jdbcTemplate;

    public JdbcGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id", GENRE_ROW_MAPPER);
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return jdbcTemplate.query("SELECT id, name FROM genres WHERE id = ?",
                        GENRE_ROW_MAPPER, id)
                .stream().findFirst();
    }
}
