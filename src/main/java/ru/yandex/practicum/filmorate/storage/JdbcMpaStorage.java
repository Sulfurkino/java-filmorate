package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMpaStorage implements MpaStorage {
    private static final MpaRowMapper MPA_ROW_MAPPER = new MpaRowMapper();
    private final JdbcTemplate jdbcTemplate;

    public JdbcMpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM mpa_ratings ORDER BY id", MPA_ROW_MAPPER);
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        return jdbcTemplate.query("SELECT id, name FROM mpa_ratings WHERE id = ?",
                        MPA_ROW_MAPPER, id)
                .stream().findFirst();
    }
}
