package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
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
        jdbcTemplate.update("INSERT INTO mpa_ratings (id, name, description) VALUES " +
                "(1, 'G', 'Test'), " +
                "(2, 'PG', 'Test'), " +
                "(3, 'PG-13', 'Test'), " +
                "(4, 'R', 'Test'), " +
                "(5, 'NC-17', 'Test')");

        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES " +
                "(1, 'Комедия'), " +
                "(2, 'Драма'), " +
                "(3, 'Мультфильм'), " +
                "(4, 'Триллер'), " +
                "(5, 'Боевик'), " +
                "(6, 'Фантастика'), " +
                "(7, 'Фэнтези'), " +
                "(8, 'Ужасы'), " +
                "(9, 'Мелодрама'), " +
                "(10, 'Приключения'), " +
                "(11, 'Семейный')");
    }

    @Test
    void testFindUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.addUser(user);
        User foundUser = userStorage.getUserById(savedUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.addUser(user);

        Collection<User> users = userStorage.getUsers();
        assertThat(users).isNotEmpty();
    }

    @Test
    void testCreateAndUpdateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.addUser(user);
        savedUser.setName("Updated Name");

        User updatedUser = userStorage.updateUser(savedUser);
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testUserNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getUserById(999L));
    }

    @Test
    void testCreateAndGetFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.addFilm(film);
        Film foundFilm = filmStorage.getFilmById(savedFilm.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testGetAllFilms() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        filmStorage.addFilm(film);

        Collection<Film> films = filmStorage.getFilms();
        assertThat(films).isNotEmpty();
    }

    @Test
    void testCreateAndUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.addFilm(film);
        savedFilm.setName("Updated Film");

        Film updatedFilm = filmStorage.updateFilm(savedFilm);
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
    }

    @Test
    void testFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.getFilmById(999L));
    }

    @Test
    void testGetAllMpaRatings() {
        List<MpaRating> ratings = filmStorage.getAllMpaRatings();
        assertThat(ratings).hasSize(5);
        assertThat(ratings.get(0).getId()).isEqualTo(1);
        assertThat(ratings.get(0).getName()).isEqualTo("G");
    }

    @Test
    void testGetMpaRatingById() {
        MpaRating rating = filmStorage.getMpaRatingById(1);
        assertThat(rating).isNotNull();
        assertThat(rating.getId()).isEqualTo(1);
        assertThat(rating.getName()).isEqualTo("G");
    }

    @Test
    void testMpaRatingNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.getMpaRatingById(999));
    }

    @Test
    void testGetAllGenres() {
        List<Genre> genres = filmStorage.getAllGenres();
        assertThat(genres).hasSize(11);
        assertThat(genres.get(0).getId()).isEqualTo(1);
        assertThat(genres.get(0).getName()).isEqualTo("Комедия");
    }

    @Test
    void testGetGenreById() {
        Genre genre = filmStorage.getGenreById(1);
        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }

    @Test
    void testGenreNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.getGenreById(999));
    }

    @Test
    void testAddAndGetFriends() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User savedUser2 = userStorage.addUser(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId(), FriendshipStatus.CONFIRMED);

        List<User> friends = userStorage.getFriends(savedUser1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(savedUser2.getId());
    }

    @Test
    void testRemoveFriend() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User savedUser2 = userStorage.addUser(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId(), FriendshipStatus.PENDING);
        userStorage.removeFriend(savedUser1.getId(), savedUser2.getId());

        List<User> friends = userStorage.getFriends(savedUser1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User savedUser2 = userStorage.addUser(user2);

        User user3 = new User();
        user3.setEmail("user3@test.com");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1993, 3, 3));
        User savedUser3 = userStorage.addUser(user3);

        userStorage.addFriend(savedUser1.getId(), savedUser3.getId(), FriendshipStatus.CONFIRMED);
        userStorage.addFriend(savedUser2.getId(), savedUser3.getId(), FriendshipStatus.CONFIRMED);

        List<User> commonFriends = userStorage.getCommonFriends(savedUser1.getId(), savedUser2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(savedUser3.getId());
    }

    @Test
    void testAddAndRemoveLike() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.addUser(user);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(Duration.ofMinutes(120));

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        Film savedFilm = filmStorage.addFilm(film);

        filmStorage.addLike(savedFilm.getId(), savedUser.getId());
        filmStorage.removeLike(savedFilm.getId(), savedUser.getId());

        Film foundFilm = filmStorage.getFilmById(savedFilm.getId());
        assertThat(foundFilm.getLikes()).isEmpty();
    }

    @Test
    void testGetPopularFilms() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.addUser(user);

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2023, 1, 1));
        film1.setDuration(Duration.ofMinutes(120));
        MpaRating mpa1 = new MpaRating();
        mpa1.setId(1);
        film1.setMpa(mpa1);
        Film savedFilm1 = filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2023, 2, 2));
        film2.setDuration(Duration.ofMinutes(90));
        MpaRating mpa2 = new MpaRating();
        mpa2.setId(2);
        film2.setMpa(mpa2);
        Film savedFilm2 = filmStorage.addFilm(film2);

        filmStorage.addLike(savedFilm1.getId(), savedUser.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(10);
        assertThat(popularFilms).isNotEmpty();
        assertThat(popularFilms.get(0).getId()).isEqualTo(savedFilm1.getId());
    }
}