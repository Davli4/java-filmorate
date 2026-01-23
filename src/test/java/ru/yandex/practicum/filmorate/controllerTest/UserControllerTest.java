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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {

    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        clearDatabase();
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

    @Test
    void shouldCreateUserWithValidData() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));

        User createdUser = userDbStorage.addUser(user);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("email@example.com");
        assertThat(createdUser.getLogin()).isEqualTo("login");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(1980, 1, 1));
        assertThat(createdUser.getName()).isEqualTo("login");
    }

    @Test
    void shouldCreateUserWithNameWhenNameIsProvided() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1980, 1, 1));

        User createdUser = userDbStorage.addUser(user);

        assertThat(createdUser.getName()).isEqualTo("User Name");
        assertThat(createdUser.getLogin()).isEqualTo("login");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("   ");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin(null);
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("   ");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginHaveSpaces() {
        User user = new User();
        user.setLogin("login asdasd");
        user.setEmail("email@example.com");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldAcceptBirthdayToday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now());

        User createdUser = userDbStorage.addUser(user);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldThrowExceptionWhenEmailWithoutAt() {
        User user = new User();
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        user.setEmail("email-example.com");
        assertThrows(ValidationException.class, () -> userDbStorage.addUser(user));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userDbStorage.addUser(user);

        createdUser.setEmail("updated@example.com");
        createdUser.setLogin("updatedlogin");
        User updatedUser = userDbStorage.updateUser(createdUser);

        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getLogin()).isEqualTo("updatedlogin");
        assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
    }

    @Test
    void shouldUpdateUserWithNullNameToUseLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userDbStorage.addUser(user);

        assertThat(createdUser.getName()).isEqualTo("testlogin");
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));

        userDbStorage.addUser(user1);
        userDbStorage.addUser(user2);

        assertThat(userDbStorage.getUsers()).hasSize(2);
    }

    @Test
    void shouldGetUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userDbStorage.addUser(user);

        User foundUser = userDbStorage.getUserById(createdUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }
}