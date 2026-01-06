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
        inMemoryUserStorage = new InMemoryUserStorage();
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
    void shouldCreateUserWithNameWhenNameIsProvided() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1980, 1, 1));

        User createdUser = inMemoryUserStorage.addUser(user);

        assertEquals("User Name", createdUser.getName());
        assertEquals("login", createdUser.getLogin());
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
    void shouldThrowExceptionWhenEmailIsEmpty() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> inMemoryUserStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("   ");
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
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        assertThrows(ValidationException.class, () -> inMemoryUserStorage.addUser(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("   ");
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
    void shouldAcceptBirthdayToday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now());

        User createdUser = inMemoryUserStorage.addUser(user);

        assertNotNull(createdUser);
        assertEquals(LocalDate.now(), createdUser.getBirthday());
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
        assertEquals(createdUser.getId(), updatedUser.getId());
    }

    @Test
    void shouldUpdateUserWithNullNameToUseLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = inMemoryUserStorage.addUser(user);

        assertEquals("testlogin", createdUser.getName());
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

        inMemoryUserStorage.addUser(user1);
        inMemoryUserStorage.addUser(user2);

        assertEquals(2, inMemoryUserStorage.getUsers().size());
    }

    @Test
    void shouldGetUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = inMemoryUserStorage.addUser(user);

        User foundUser = inMemoryUserStorage.getUserById(createdUser.getId());
        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("test@example.com", foundUser.getEmail());
    }
}