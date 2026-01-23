package ru.yandex.practicum.filmorate.controllerTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmControllerTest {

    private final FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clearDatabase();
        insertTestData();
    }

    private void clearDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM mpa_ratings");

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        jdbcTemplate.execute("ALTER TABLE mpa_ratings ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE genres ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
    }

    private void insertTestData() {
        jdbcTemplate.update("INSERT INTO mpa_ratings (id, name, description) VALUES (1, 'G', 'Test')");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (1, 'Test Genre')");
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmDbStorage.addFilm(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getDescription()).isEqualTo("Test Description");
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmDbStorage.addFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmDbStorage.addFilm(film));
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmDbStorage.addFilm(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getDescription()).hasSize(200);
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeCinemaDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(Duration.ofMinutes(120));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmDbStorage.addFilm(film));
    }

    @Test
    void shouldAcceptReleaseDateOnCinemaDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(120));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmDbStorage.addFilm(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(-10));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        assertThrows(ValidationException.class, () -> filmDbStorage.addFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenFilmNotFoundForUpdate() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        assertThrows(NotFoundException.class, () -> filmDbStorage.updateFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenIdIsNullForUpdate() {
        Film film = new Film();
        film.setId(null);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        assertThrows(NotFoundException.class, () -> filmDbStorage.updateFilm(film));
    }

    @Test
    void shouldCreateFilmWithDifferentMpaRatings() {
        jdbcTemplate.update("INSERT INTO mpa_ratings (id, name, description) VALUES (2, 'PG', 'Test PG')");

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(Duration.ofMinutes(120));
        MpaRating mpa1 = new MpaRating();
        mpa1.setId(1);
        film1.setMpa(mpa1);

        Film createdFilm1 = filmDbStorage.addFilm(film1);
        assertThat(createdFilm1).isNotNull();
        assertThat(createdFilm1.getId()).isNotNull();
        assertThat(createdFilm1.getMpa().getId()).isEqualTo(1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(Duration.ofMinutes(90));
        MpaRating mpa2 = new MpaRating();
        mpa2.setId(2);
        film2.setMpa(mpa2);

        Film createdFilm2 = filmDbStorage.addFilm(film2);
        assertThat(createdFilm2).isNotNull();
        assertThat(createdFilm2.getId()).isNotNull();
        assertThat(createdFilm2.getMpa().getId()).isEqualTo(2);
    }
}