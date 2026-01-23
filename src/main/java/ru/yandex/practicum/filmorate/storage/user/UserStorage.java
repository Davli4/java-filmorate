package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    Collection<User> getUsers();

    User addUser(User user);

    User updateUser(User user);

    User getUserById(Long id);

    boolean existsById(Long id);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId1, Long userId2);

    void addFriend(Long userId, Long friendId, FriendshipStatus status);

    void removeFriend(Long userId, Long friendId);

    List<User> getPendingFriendRequests(Long userId);
}