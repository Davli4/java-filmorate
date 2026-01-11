package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private InMemoryFilmStorage inMemoryFilmStorage;

    @BeforeEach
    void setUp() {
        inMemoryFilmStorage = new InMemoryFilmStorage();
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        film.setMpa(Rating.PG_13);

        Film createdFilm = inMemoryFilmStorage.addFilm(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals("Test Description", createdFilm.getDescription());
        assertEquals(Rating.PG_13, createdFilm.getMpa());
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        film.setMpa(Rating.PG_13);

        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        film.setMpa(Rating.PG_13);

        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(film));
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));
        film.setMpa(Rating.PG_13);

        Film createdFilm = inMemoryFilmStorage.addFilm(film);

        assertNotNull(createdFilm);
        assertEquals(200, createdFilm.getDescription().length());
        assertEquals(Rating.PG_13, createdFilm.getMpa());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateBeforeCinemaDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(Duration.ofMinutes(120));
        film.setMpa(Rating.PG_13);

        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(film));
    }

    @Test
    void shouldAcceptReleaseDateOnCinemaDate() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(Duration.ofMinutes(120));
        film.setMpa(Rating.G);

        Film createdFilm = inMemoryFilmStorage.addFilm(film);

        assertNotNull(createdFilm);
        assertEquals(Rating.G, createdFilm.getMpa());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(-10));
        film.setMpa(Rating.PG_13);

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
        film.setMpa(Rating.PG_13);

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
        film.setMpa(Rating.PG_13);

        assertThrows(NotFoundException.class, () -> inMemoryFilmStorage.updateFilm(film));
    }

    @Test
    void shouldThrowExceptionWhenMpaRatingIsNull() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(film));
    }

    @Test
    void shouldCreateFilmWithDifferentMpaRatings() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(Duration.ofMinutes(120));
        film1.setMpa(Rating.R);

        Film createdFilm1 = inMemoryFilmStorage.addFilm(film1);
        assertNotNull(createdFilm1.getId());
        assertEquals(Rating.R, createdFilm1.getMpa());

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(Duration.ofMinutes(90));
        film2.setMpa(Rating.NC_17);

        Film createdFilm2 = inMemoryFilmStorage.addFilm(film2);
        assertNotNull(createdFilm2.getId());
        assertEquals(Rating.NC_17, createdFilm2.getMpa());
    }
}