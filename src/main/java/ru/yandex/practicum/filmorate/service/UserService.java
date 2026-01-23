package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("User cannot add himself as a friend");
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (userStorage instanceof UserDbStorage) {
            UserDbStorage userDbStorage = (UserDbStorage) userStorage;

            if (friend.getFriends().containsKey(userId)) {
                userDbStorage.addFriend(userId, friendId, FriendshipStatus.CONFIRMED);
                userDbStorage.addFriend(friendId, userId, FriendshipStatus.CONFIRMED);
                log.info("Friendship between {} and {} is confirmed", userId, friendId);
            } else {
                userDbStorage.addFriend(userId, friendId, FriendshipStatus.PENDING);
                log.info("User {} sent friend request to {}", userId, friendId);
            }
        } else {
            user.addFriend(friendId, FriendshipStatus.PENDING);

            if (friend.getFriends().containsKey(userId)) {
                user.addFriend(friendId, FriendshipStatus.CONFIRMED);
                friend.addFriend(userId, FriendshipStatus.CONFIRMED);
                log.info("Friendship between {} and {} is confirmed", userId, friendId);
            }
        }

        log.info("User {} added friend {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (userStorage instanceof UserDbStorage) {
            UserDbStorage userDbStorage = (UserDbStorage) userStorage;
            userDbStorage.removeFriend(userId, friendId);
        } else {
            if (user.getFriends().remove(friendId) == null) {
                log.warn("User {} is not friend with {}", userId, friendId);
            }
            friend.getFriends().remove(userId);
        }

        log.info("User {} removed friend {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        if (userStorage instanceof UserDbStorage) {
            return ((UserDbStorage) userStorage).getFriends(userId);
        } else {
            User user = getUserById(userId);
            return user.getFriendIds().stream()
                    .map(this::getUserById)
                    .collect(Collectors.toList());
        }
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        if (userStorage instanceof UserDbStorage) {
            return ((UserDbStorage) userStorage).getCommonFriends(userId1, userId2);
        } else {
            User user1 = getUserById(userId1);
            User user2 = getUserById(userId2);

            Set<Long> commonFriendIds = new HashSet<>(user1.getFriendIds());
            commonFriendIds.retainAll(user2.getFriendIds());

            return commonFriendIds.stream()
                    .map(this::getUserById)
                    .collect(Collectors.toList());
        }
    }
}