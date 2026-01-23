package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.mapper.UserMapper;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = new UserMapper();
        log.debug("UserDbStorage initialized with JdbcTemplate");
    }

    @Override
    public Collection<User> getUsers() {
        log.debug("Getting all users from database");
        String sql = "SELECT * FROM users ORDER BY id";
        try {
            List<User> users = jdbcTemplate.query(sql, userMapper);
            log.debug("Retrieved {} users from database", users.size());
            users.forEach(this::loadUserFriends);
            return users;
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public User addUser(User user) {
        log.debug("Adding new user: email={}, login={}", user.getEmail(), user.getLogin());
        validateUser(user);

        try {
            log.debug("Checking if users table exists");
            jdbcTemplate.execute("SELECT 1 FROM users LIMIT 1");
            log.debug("Users table exists");
        } catch (Exception e) {
            log.error("Users table does not exist or is not accessible: {}", e.getMessage());
            throw new RuntimeException("Таблица users не существует или недоступна", e);
        }

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday());

        log.debug("Inserting user with parameters: {}", parameters);
        try {
            Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
            user.setId(generatedId.longValue());
            log.info("User added with id: {}", user.getId());
            return getUserById(user.getId());
        } catch (Exception e) {
            log.error("Error adding user: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public User updateUser(User user) {
        log.debug("Updating user with id: {}", user.getId());

        if (user.getId() == null || !existsById(user.getId())) {
            log.warn("User with ID {} not found for update", user.getId());
            throw new NotFoundException("User with ID " + user.getId() + " not found");
        }

        validateUser(user);

        String sql = """
            UPDATE users
            SET email = ?, login = ?, name = ?, birthday = ?
            WHERE id = ?
            """;

        log.debug("Executing update SQL: {} with params: email={}, login={}, name={}, birthday={}, id={}",
                sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        try {
            int rowsUpdated = jdbcTemplate.update(sql,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId()
            );

            log.debug("Updated {} rows for user id: {}", rowsUpdated, user.getId());
            log.info("User updated with id: {}", user.getId());
            return getUserById(user.getId());
        } catch (Exception e) {
            log.error("Error updating user with id {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public User getUserById(Long id) {
        log.debug("Getting user by id: {}", id);
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userMapper, id);
            log.debug("Found user with id {}: {}", id, user);
            loadUserFriends(user);
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.warn("User with ID {} not found", id);
            throw new NotFoundException("User with ID " + id + " not found");
        } catch (Exception e) {
            log.error("Error getting user by id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean existsById(Long id) {
        log.debug("Checking if user exists with id: {}", id);
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
            boolean exists = count != null && count > 0;
            log.debug("User with id {} exists: {}", id, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error checking if user exists with id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    private void loadUserFriends(User user) {
        if (user == null) {
            log.warn("Attempted to load friends for null user");
            return;
        }

        log.debug("Loading friends for user id: {}", user.getId());
        String sql = """
            SELECT friend_id, status
            FROM friendships
            WHERE user_id = ?
            """;

        try {
            jdbcTemplate.query(sql, rs -> {
                Long friendId = rs.getLong("friend_id");
                String status = rs.getString("status");
                user.getFriends().put(friendId, FriendshipStatus.valueOf(status));
                log.debug("Added friend {} with status {} to user {}", friendId, status, user.getId());
            }, user.getId());
            log.debug("Loaded {} friends for user id: {}", user.getFriends().size(), user.getId());
        } catch (Exception e) {
            log.error("Error loading friends for user id {}: {}", user.getId(), e.getMessage(), e);
        }
    }

    private void validateUser(User user) {
        log.debug("Validating user: {}", user);

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("User validation failed: invalid email - {}", user.getEmail());
            throw new ValidationException("Email is mandatory and must contain @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("User validation failed: invalid login - {}", user.getLogin());
            throw new ValidationException("Login is mandatory and cannot contain spaces");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("User validation failed: invalid birthday - {}", user.getBirthday());
            throw new ValidationException("Birthday cannot be in the future");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("User name is empty, setting to login: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        log.debug("User validation passed");
    }

    @Override
    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        log.debug("Adding friend: user={}, friend={}, status={}", userId, friendId, status);

        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        try {
            int rowsInserted = jdbcTemplate.update(sql, userId, friendId, status.toString());
            log.debug("Inserted {} rows into friendships", rowsInserted);

            if (status == FriendshipStatus.CONFIRMED) {
                int reverseRows = jdbcTemplate.update(sql, friendId, userId, status.toString());
                log.debug("Inserted {} reverse friendship rows", reverseRows);
            }
            log.info("Added friend {} to user {} with status {}", friendId, userId, status);
        } catch (Exception e) {
            log.warn("Failed to insert friendship, trying update: {}", e.getMessage());
            sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
            int rowsUpdated = jdbcTemplate.update(sql, status.toString(), userId, friendId);
            log.debug("Updated {} friendship rows", rowsUpdated);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        log.debug("Removing friend: user={}, friend={}", userId, friendId);

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            int rowsDeleted = jdbcTemplate.update(sql, userId, friendId);
            log.debug("Deleted {} friendship rows", rowsDeleted);
            log.info("Removed friend {} from user {}", friendId, userId);
        } catch (Exception e) {
            log.error("Error removing friend {} from user {}: {}", friendId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        log.debug("Getting friends for user id: {}", userId);

        String sql = """
            SELECT u.*
            FROM users u
            JOIN friendships f ON u.id = f.friend_id
            WHERE f.user_id = ?
            ORDER BY u.id
            """;

        try {
            List<User> friends = jdbcTemplate.query(sql, userMapper, userId);
            log.debug("Found {} friends for user id: {}", friends.size(), userId);
            return friends;
        } catch (Exception e) {
            log.error("Error getting friends for user id {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        log.debug("Getting common friends for users: {} and {}", userId1, userId2);

        String sql = """
            SELECT u.*
            FROM users u
            JOIN friendships f1 ON u.id = f1.friend_id
            JOIN friendships f2 ON u.id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            ORDER BY u.id
            """;

        try {
            List<User> commonFriends = jdbcTemplate.query(sql, userMapper, userId1, userId2);
            log.debug("Found {} common friends for users {} and {}", commonFriends.size(), userId1, userId2);
            return commonFriends;
        } catch (Exception e) {
            log.error("Error getting common friends for users {} and {}: {}", userId1, userId2, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<User> getPendingFriendRequests(Long userId) {
        log.debug("Getting pending friend requests for user id: {}", userId);

        String sql = """
            SELECT u.*
            FROM users u
            JOIN friendships f ON u.id = f.friend_id
            WHERE f.user_id = ? AND f.status = 'PENDING'
            ORDER BY u.id
            """;

        try {
            List<User> pendingRequests = jdbcTemplate.query(sql, userMapper, userId);
            log.debug("Found {} pending friend requests for user id: {}", pendingRequests.size(), userId);
            return pendingRequests;
        } catch (Exception e) {
            log.error("Error getting pending friend requests for user id {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}