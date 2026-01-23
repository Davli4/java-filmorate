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
    }

    @Override
    public Collection<User> getUsers() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = jdbcTemplate.query(sql, userMapper);
        users.forEach(this::loadUserFriends);
        return users;
    }

    @Override
    public User addUser(User user) {
        validateUser(user);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday());

        Number generatedId = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(generatedId.longValue());

        log.info("User added with id: {}", user.getId());
        return getUserById(user.getId());
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null || !existsById(user.getId())) {
            throw new NotFoundException("User with ID " + user.getId() + " not found");
        }
        validateUser(user);

        String sql = """
            UPDATE users 
            SET email = ?, login = ?, name = ?, birthday = ?
            WHERE id = ?
            """;

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        log.info("User updated with id: {}", user.getId());
        return getUserById(user.getId());
    }

    @Override
    public User getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userMapper, id);
            loadUserFriends(user);
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("User with ID " + id + " not found");
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private void loadUserFriends(User user) {
        if (user == null) return;

        String sql = """
            SELECT friend_id, status
            FROM friendships
            WHERE user_id = ?
            """;

        jdbcTemplate.query(sql, rs -> {
            Long friendId = rs.getLong("friend_id");
            String status = rs.getString("status");
            user.getFriends().put(friendId, FriendshipStatus.valueOf(status));
        }, user.getId());
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email is mandatory and must contain @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Login is mandatory and cannot contain spaces");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Birthday cannot be in the future");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, userId, friendId, status.toString());

            if (status == FriendshipStatus.CONFIRMED) {
                jdbcTemplate.update(sql, friendId, userId, status.toString());
            }
        } catch (Exception e) {
            sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(sql, status.toString(), userId, friendId);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = """
            SELECT u.* 
            FROM users u
            JOIN friendships f ON u.id = f.friend_id
            WHERE f.user_id = ?
            ORDER BY u.id
            """;

        return jdbcTemplate.query(sql, userMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        String sql = """
            SELECT u.*
            FROM users u
            JOIN friendships f1 ON u.id = f1.friend_id
            JOIN friendships f2 ON u.id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            ORDER BY u.id
            """;

        return jdbcTemplate.query(sql, userMapper, userId1, userId2);
    }

    @Override
    public List<User> getPendingFriendRequests(Long userId) {
        String sql = """
            SELECT u.*
            FROM users u
            JOIN friendships f ON u.id = f.friend_id
            WHERE f.user_id = ? AND f.status = 'PENDING'
            ORDER BY u.id
            """;

        return jdbcTemplate.query(sql, userMapper, userId);
    }
}