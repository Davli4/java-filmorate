package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final InMemoryUserStorage userStorage;

    public UserService(InMemoryUserStorage userStorage) {
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

        user.addFriend(friendId, FriendshipStatus.PENDING);

        if(friend.getFriends().containsKey(userId)) {
            user.addFriend(friendId, FriendshipStatus.CONFIRMED);
            friend.addFriend(userId, FriendshipStatus.CONFIRMED);
            log.info("Friendship between {} and {} is confirmed", userId, friendId);
        } else {
            log.info("User {} sent friend request to {}", userId, friendId);
        }

        log.info("User {} added friend {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends().remove(friendId) == null) {
            log.warn("User {} is not friend with {}", userId, friendId);
        }
        friend.getFriends().remove(userId);

        log.info("User {} removed friend {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        List<User> friends = new ArrayList<>();

        for (Long friendId : user.getFriendIds()) {
            friends.add(getUserById(friendId));
        }

        return friends;
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        User user1 = getUserById(userId1);
        User user2 = getUserById(userId2);

        Set<Long> commonFriendIds = new HashSet<>(user1.getFriendIds());
        commonFriendIds.retainAll(user2.getFriendIds());

        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : commonFriendIds) {
            commonFriends.add(getUserById(friendId));
        }

        return commonFriends;
    }

    public void confirmFriendRequest(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if(!user.getFriendIds().contains(friendId)) {
            throw new ValidationException("User is not friend with " + friendId);
        }

        user.addFriend(friendId, FriendshipStatus.CONFIRMED);
        friend.addFriend(userId, FriendshipStatus.CONFIRMED);
        log.info("User {} confirmed friend request from {}", userId, friendId);
    }

    public List<User> getPendingFriendRequest(Long userId){
            User user = getUserById(userId);
            return user.getFriends().entrySet().stream()
                    .filter(entry -> entry.getValue().equals(FriendshipStatus.PENDING))
                    .map(entry -> getUserById(entry.getKey()))
                    .collect(Collectors.toList());
    }
}