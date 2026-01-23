package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.film.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.film.mapper.MpaRatingMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;
    private final MpaRatingMapper mpaRatingMapper;
    private final GenreMapper genreMapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmMapper = new FilmMapper();
        this.mpaRatingMapper = new MpaRatingMapper();
        this.genreMapper = new GenreMapper();
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = """
            SELECT f.*, m.name as mpa_name, m.description as mpa_description
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            ORDER BY f.id
            """;

        List<Film> films = jdbcTemplate.query(sql, filmMapper);
        films.forEach(this::loadFilmData);
        return films;
    }

    @Override
    public Film addFilm(Film film) {
        validateFilm(film);

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            String checkMpaSql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
            Integer mpaCount = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, film.getMpa().getId());
            if (mpaCount == null || mpaCount == 0) {
                throw new NotFoundException("MPA rating with ID " + film.getMpa().getId() + " not found");
            }
        } else {
            throw new ValidationException("MPA rating is required");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            String checkGenresSql = "SELECT COUNT(*) FROM genres WHERE id IN (" +
                    genreIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
            Integer genresCount = jdbcTemplate.queryForObject(checkGenresSql, Integer.class);

            if (genresCount == null || genresCount != genreIds.size()) {
                throw new NotFoundException("One or more genres not found");
            }
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("mpa_rating_id", film.getMpa() != null ? film.getMpa().getId() : null);

        Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
        film.setId(generatedId.longValue());

        saveFilmGenres(film);

        log.info("Film added with id: {}", film.getId());
        return getFilmById(film.getId());
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null || !existsById(film.getId())) {
            throw new NotFoundException("Film ID not found");
        }
        validateFilm(film);

        String sql = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?,
                duration = ?, mpa_rating_id = ?
            WHERE id = ?
            """;

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveFilmGenres(film);

        log.info("Film updated with id: {}", film.getId());
        return getFilmById(film.getId());
    }

    @Override
    public Film getFilmById(Long id) {
        String sql = """
            SELECT f.*, m.name as mpa_name, m.description as mpa_description
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            WHERE f.id = ?
            """;

        try {
            Film film = jdbcTemplate.queryForObject(sql, filmMapper, id);
            loadFilmData(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Film with ID " + id + " not found");
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> batchArgs = film.getGenres().stream()
                    .map(genre -> new Object[]{film.getId(), genre.getId()})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    batchArgs
            );
        }
    }

    private void loadFilmData(Film film) {
        if (film == null) return;

        String genresSql = """
        SELECT g.*
        FROM genres g
        JOIN film_genres fg ON g.id = fg.genre_id
        WHERE fg.film_id = ?
        ORDER BY g.id
        """;

        List<Genre> genres = jdbcTemplate.query(genresSql, genreMapper, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));

        String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(likesSql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
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
    }

    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (Exception e) {
            log.warn("User {} already liked film {}", userId, filmId);
        }
    }

    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(sql, filmId, userId);
        if (deleted == 0) {
            throw new NotFoundException("Like not found for user " + userId + " and film " + filmId);
        }
    }

    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mpaRatingMapper);
    }

    public MpaRating getMpaRatingById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, mpaRatingMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA rating with ID " + id + " not found");
        }
    }

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreMapper);
    }

    public Genre getGenreById(Integer id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, genreMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Genre with ID " + id + " not found");
        }
    }

    public List<Film> getPopularFilms(int count) {
        String sql = """
            SELECT f.*, m.name as mpa_name, m.description as mpa_description,
                   COUNT(l.user_id) as likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            GROUP BY f.id, m.id
            ORDER BY likes_count DESC
            LIMIT ?
            """;

        List<Film> films = jdbcTemplate.query(sql, filmMapper, count);
        films.forEach(this::loadFilmData);
        return films;
    }
}