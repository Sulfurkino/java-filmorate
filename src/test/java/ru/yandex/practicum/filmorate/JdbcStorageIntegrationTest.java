package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcStorageIntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private GenreStorage genreStorage;
    @Autowired
    private MpaStorage mpaStorage;
    @Autowired
    private FilmService filmService;
    @Autowired
    private UserService userService;

    @BeforeEach
    void clearUserData() {
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM user_friends");
        jdbcTemplate.update("DELETE FROM films_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void shouldCrudUsersAndFilmsWithReferences() {
        User user = userStorage.create(newUser("first"));
        assertThat(user.getId()).isNotNull();
        user.setName("Updated");
        assertThat(userStorage.update(user).getName()).isEqualTo("Updated");

        Film film = filmStorage.create(newFilm("Film", 1L, 1L, 2L));
        Film stored = filmStorage.findById(film.getId()).orElseThrow();
        assertThat(stored.getMpa()).isEqualTo(new Mpa(1L, "G"));
        assertThat(stored.getGenres()).extracting(Genre::getId).containsExactly(1L, 2L);

        stored.setName("Updated film");
        stored.setGenres(new LinkedHashSet<>(List.of(new Genre(6L, null))));
        Film updated = filmStorage.update(stored);
        assertThat(updated.getName()).isEqualTo("Updated film");
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(6L);
    }

    @Test
    void shouldReadSeededGenresAndMpaRatings() {
        assertThat(genreStorage.getAll()).hasSize(6);
        assertThat(genreStorage.findById(1L)).hasValue(new Genre(1L, "Комедия"));
        assertThat(mpaStorage.getAll()).hasSize(5);
        assertThat(mpaStorage.findById(5L)).hasValue(new Mpa(5L, "NC-17"));
    }

    @Test
    void shouldMapLikesAndSortPopularDeterministically() {
        User firstUser = userStorage.create(newUser("first"));
        User secondUser = userStorage.create(newUser("second"));
        Film firstFilm = filmService.create(newFilm("First", 1L));
        Film secondFilm = filmService.create(newFilm("Second", 1L));
        Film thirdFilm = filmService.create(newFilm("Third", 1L));

        filmService.addLike(secondFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), firstUser.getId());
        filmService.addLike(firstFilm.getId(), secondUser.getId());

        assertThat(filmStorage.findById(secondFilm.getId()).orElseThrow().getLikes())
                .containsExactly(firstUser.getId());
        assertThat(filmService.getPopular(3)).extracting(Film::getId)
                .containsExactly(firstFilm.getId(), secondFilm.getId(), thirdFilm.getId());
    }

    @Test
    void shouldKeepFriendshipDirectionalAndFindCommonFriends() {
        User first = userStorage.create(newUser("first"));
        User second = userStorage.create(newUser("second"));
        User common = userStorage.create(newUser("common"));

        userService.addFriend(first.getId(), common.getId());
        userService.addFriend(first.getId(), common.getId());
        userService.addFriend(second.getId(), common.getId());

        assertThat(userService.getFriends(common.getId())).isEmpty();
        assertThat(userService.getCommonFriends(first.getId(), second.getId()))
                .extracting(User::getId).containsExactly(common.getId());

        userService.addFriend(common.getId(), first.getId());
        userService.removeFriend(first.getId(), common.getId());
        assertThat(userService.getFriends(first.getId())).isEmpty();
        assertThat(userService.getFriends(common.getId())).extracting(User::getId)
                .containsExactly(first.getId());
    }

    private User newUser(String suffix) {
        return User.builder()
                .email(suffix + "@example.com")
                .login(suffix)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    private Film newFilm(String name, Long mpaId, Long... genreIds) {
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        for (Long genreId : genreIds) {
            genres.add(new Genre(genreId, null));
        }
        return Film.builder()
                .name(name)
                .description(null)
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100)
                .mpa(new Mpa(mpaId, null))
                .genres(genres)
                .build();
    }
}
