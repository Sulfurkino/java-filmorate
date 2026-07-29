package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcUserStorage implements UserStorage {
    private static final UserRowMapper USER_ROW_MAPPER = new UserRowMapper();
    private static final String SELECT_USERS = "SELECT id, email, login, name, birthday FROM users";

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getAll() {
        List<User> users = jdbcTemplate.query(SELECT_USERS + " ORDER BY id", USER_ROW_MAPPER);
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query(SELECT_USERS + " WHERE id = ?", USER_ROW_MAPPER, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        User user = users.getFirst();
        loadFriends(user);
        return Optional.of(user);
    }

    @Override
    public User create(User user) {
        useLoginAsDefaultName(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setObject(4, user.getBirthday());
            return statement;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return findById(user.getId()).orElseThrow();
    }

    @Override
    public User update(User user) {
        useLoginAsDefaultName(user);
        jdbcTemplate.update("""
                UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?
                """, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return findById(user.getId()).orElseThrow();
    }

    @Override
    public boolean addFriend(Long id, Long friendId) {
        return jdbcTemplate.update("""
                MERGE INTO user_friends (user_id, friend_id, status) KEY(user_id, friend_id)
                VALUES (?, ?, TRUE)
                """, id, friendId) > 0;
    }

    @Override
    public boolean removeFriend(Long id, Long friendId) {
        return jdbcTemplate.update(
                "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?",
                id, friendId) > 0;
    }

    @Override
    public List<User> getFriends(Long id) {
        return queryUsers("""
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                JOIN user_friends uf ON uf.friend_id = u.id
                WHERE uf.user_id = ? AND uf.status = TRUE
                ORDER BY u.id
                """, id);
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        return queryUsers("""
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                JOIN user_friends first_friend ON first_friend.friend_id = u.id
                JOIN user_friends second_friend ON second_friend.friend_id = u.id
                WHERE first_friend.user_id = ? AND second_friend.user_id = ?
                  AND first_friend.status = TRUE AND second_friend.status = TRUE
                ORDER BY u.id
                """, id, otherId);
    }

    private List<User> queryUsers(String sql, Object... arguments) {
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, arguments);
        users.forEach(this::loadFriends);
        return users;
    }

    private void loadFriends(User user) {
        user.getFriends().addAll(jdbcTemplate.queryForList("""
                SELECT friend_id FROM user_friends
                WHERE user_id = ? AND status = TRUE ORDER BY friend_id
                """, Long.class, user.getId()));
    }

    private void useLoginAsDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
