package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final HashMap<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Get request, size: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("Add request, user: {}", user);

        try {
            log.info("Starting to validation users");
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                String errorMessage = "Email is mandatory";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                String errorMessage = "Login is mandatory";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }

            if (user.getBirthday().isAfter(LocalDate.now())) {
                String errorMessage = "Birthday is after current date";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            long userId = generateUserId();
            user.setId(userId);
            users.put(userId, user);

            return user;
        } catch (ValidationException e) {
            log.error("Error adding user: {}, case {}", user, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error to adding user: {}", user, e);
            throw e;
        }

    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Update request, user: {}", user);

        try {
            log.info("Starting to validation users");
            if (user.getId() == null) {
                String errorMessage = "ID is required for update";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }

            if (!users.containsKey(user.getId())) {
                String errorMessage = "User with ID " + user.getId() + " not found";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                String errorMessage = "Email is mandatory and cannot be empty";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            if (!user.getEmail().contains("@")) {
                String errorMessage = "Email must contain @ symbol";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }

            if (user.getLogin() == null || user.getLogin().isBlank()) {
                String errorMessage = "Login is mandatory and cannot be empty";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            if (user.getLogin().contains(" ")) {
                String errorMessage = "Login cannot contain spaces";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }

            if (user.getBirthday() == null) {
                String errorMessage = "Birthday is mandatory";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            if (user.getBirthday().isAfter(LocalDate.now())) {
                String errorMessage = "Birthday cannot be in the future";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }

            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }

            users.put(user.getId(), user);
            return user;
        } catch (ValidationException e) {
            log.error("Error updating user: {}, case {}", user, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error to updating user: {}", user, e);
            throw e;
        }
    }

    private long generateUserId() {
        long currentId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++currentId;
    }

}