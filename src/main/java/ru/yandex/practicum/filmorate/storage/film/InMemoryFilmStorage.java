package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    @Override
    public Collection<Film> getFilms() {
        log.info("Get request, films size: {}", films.size());
        return films.values();
    }

    @Override
    public Film addFilm(Film film) {
        validateFilm(film);
        film.setId(++currentId);
        films.put(film.getId(), film);
        log.info("Film added: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            throw new NotFoundException("Film ID not found");
        }
        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Film updated: {}", film);
        return film;
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Film with ID " + id + " not found");
        }
        return film;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Film name cannot be empty");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Description cannot be longer than 200 characters");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Release date cannot be before 28.12.1895");
        }
        if (film.getDuration() == null || film.getDuration().toMinutes() <= 0) {
            throw new ValidationException("Duration must be positive");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("MPA rating is required");
        }
    }
}