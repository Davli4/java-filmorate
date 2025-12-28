package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController userController;

    @BeforeEach
    public void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldCreateUserWithValidData() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));

        User createdUser = userController.addUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("email@example.com", user.getEmail());
        assertEquals("login", user.getLogin());
        assertEquals(LocalDate.of(1980, 1, 1), user.getBirthday());
        assertEquals("login", user.getName());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginHaveSpaces() {
        User user = new User();
        user.setLogin("login asdasd");
        user.setEmail("email@example.com");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailWithoutAt() {
        User user = new User();
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        user.setEmail("email-example.com");
        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.addUser(user);

        createdUser.setEmail("updated@example.com");
        createdUser.setLogin("updatedlogin");
        User updatedUser = userController.updateUser(createdUser);

        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("updatedlogin", updatedUser.getLogin());
    }
}