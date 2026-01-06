package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final InMemoryFilmStorage filmStorage;
    private final InMemoryUserStorage userStorage;

    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getFilms();
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        if (film.getLikes().contains(userId)) {
            log.warn("User {} already liked film {}", userId, filmId);
            return;
        }

        film.getLikes().add(userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        if (!film.getLikes().remove(userId)) {
            log.warn("User {} didn't like film {}", userId, filmId);
            throw new NotFoundException("Like not found for user " + userId + " and film " + filmId);
        }

        log.info("User {} removed like from film {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;

        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size())
                        .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public int getLikesCount(Long filmId) {
        Film film = filmStorage.getFilmById(filmId);
        return film.getLikes().size();
    }

    public void addGenreFromFilm(Long filmId, Genre genre) {
        Film film = filmStorage.getFilmById(filmId);
        film.getGenres().add(genre);
        log.info("Film {} has genre {}", filmId, genre);
    }

    public void removeGenreFromFilm(Long filmId, Genre genre) {
        Film film = filmStorage.getFilmById(filmId);
        film.getGenres().remove(genre);
        log.info("From film {} was removed genre {}", filmId, genre);
    }

    public Set<Genre> getFilmGenres(Long filmId) {
        Film film = filmStorage.getFilmById(filmId);
        return film.getGenres();
    }
}