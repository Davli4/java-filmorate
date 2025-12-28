package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private InMemoryUserStorage inMemoryUserStorage;

    @BeforeEach
    public void setUp() {
        inMemoryUserStorage = new InMemoryUserStorage(); // Добавлена инициализация
    }

    @Test
    void shouldCreateUserWithValidData() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));

        User createdUser = inMemoryUserStorage.addUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("email@example.com", createdUser.getEmail());
        assertEquals("login", createdUser.getLogin());
        assertEquals(LocalDate.of(1980, 1, 1), createdUser.getBirthday());
        assertEquals("login", createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> inMemoryUserStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin(null);
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> inMemoryUserStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginHaveSpaces() {
        User user = new User();
        user.setLogin("login asdasd");
        user.setEmail("email@example.com");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> inMemoryUserStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> inMemoryUserStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailWithoutAt() {
        User user = new User();
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        user.setEmail("email-example.com");
        assertThrows(ValidationException.class, () -> inMemoryUserStorage.addUser(user));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = inMemoryUserStorage.addUser(user);

        createdUser.setEmail("updated@example.com");
        createdUser.setLogin("updatedlogin");
        User updatedUser = inMemoryUserStorage.updateUser(createdUser);

        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("updatedlogin", updatedUser.getLogin());
    }
}