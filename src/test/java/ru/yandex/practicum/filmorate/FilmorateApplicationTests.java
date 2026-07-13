package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FilmorateApplicationTests {

	@Test
	void contextLoads() {
	}

	private Validator validator;

	@BeforeEach
	public void setUp() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@Test
	void shouldAccessCreateUser() {
		User user = User.builder()
				.email("abcdef@mail.com")
				.login("abcd")
				.name("Alphabet")
				.birthday(LocalDate.of(2000, 12, 1))
				.build();
		Set<ConstraintViolation<User>> errors = validator.validate(user);
		assertTrue(errors.isEmpty());
	}

	@Test
	void shouldFailCreateUser() {
		User user = User.builder()
				.email("abcdef mail.com")
				.login("ab cd")
				.name("Alph abet")
				.birthday(LocalDate.of(2900, 12, 1))
				.build();
		Set<ConstraintViolation<User>> errors = validator.validate(user);
		assertFalse(errors.isEmpty());
	}

	@Test
	void shouldValidateCorrectFilm() {
		Film film = Film.builder()
				.name("Film")
				.description("Description")
				.releaseDate(LocalDate.of(1895, 12, 28))
				.duration(120)
				.build();

		Set<ConstraintViolation<Film>> errors = validator.validate(film);

		assertTrue(errors.isEmpty());
	}

	@Test
	void shouldFailIncorrectFilm() {
		Film film = Film.builder()
				.name("")
				.description("")
				.releaseDate(LocalDate.of(1895, 12, 27))
				.duration(0)
				.build();

		Set<ConstraintViolation<Film>> errors = validator.validate(film);

		assertFalse(errors.isEmpty());
	}

	@Test
	void shouldAcceptDescriptionWith200Characters() {
		String description = "a".repeat(200);

		Film film = Film.builder()
				.name("Film")
				.description(description)
				.releaseDate(LocalDate.of(1895, 12, 28))
				.duration(120)
				.build();

		Set<ConstraintViolation<Film>> errors = validator.validate(film);

		assertTrue(errors.isEmpty());
	}

	@Test
	void shouldRejectDescriptionLongerThan200Characters() {
		String description = "a".repeat(201);

		Film film = Film.builder()
				.name("Film")
				.description(description)
				.releaseDate(LocalDate.of(1895, 12, 28))
				.duration(120)
				.build();

		Set<ConstraintViolation<Film>> errors = validator.validate(film);

		assertFalse(errors.isEmpty());
	}

	@Test
	void shouldAcceptPositiveDuration() {
		Film film = Film.builder()
				.name("Film")
				.description("Description")
				.releaseDate(LocalDate.of(1895, 12, 28))
				.duration(1)
				.build();

		Set<ConstraintViolation<Film>> errors = validator.validate(film);

		assertTrue(errors.isEmpty());
	}


}
