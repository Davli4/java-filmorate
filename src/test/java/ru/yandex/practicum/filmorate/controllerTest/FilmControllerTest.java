package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private InMemoryFilmStorage inMemoryFilmStorage;

    @BeforeEach
    void setUp() {
        inMemoryFilmStorage = new InMemoryFilmStorage(); // Изменено на InMemoryFilmStorage
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        Film createdFilm = inMemoryFilmStorage.addFilm(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals("Test Description", createdFilm.getDescription());
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(film));
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        Film createdFilm = inMemoryFilmStorage.addFilm(film);

        assertNotNull(createdFilm);
        assertEquals(200, createdFilm.getDescription().length());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeCinemaDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(film));
    }

    @Test
    void shouldAcceptReleaseDateOnCinemaDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(120));

        Film createdFilm = inMemoryFilmStorage.addFilm(film);

        assertNotNull(createdFilm);
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(-10));

        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenFilmNotFoundForUpdate() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(NotFoundException.class, () -> inMemoryFilmStorage.updateFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenIdIsNullForUpdate() {
        Film film = new Film();
        film.setId(null);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(NotFoundException.class, () -> inMemoryFilmStorage.updateFilm(film));
    }
}