package ru.practicum.shareit.user.dal;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryImpl implements UserRepository {
    private final List<User> users = new ArrayList<>();

    @Override
    public User createUser(User user) {
        emailExists(user.getEmail());
        user.setId(getId());
        users.add(user);
        return user;
    }

    @Override
    public User updateUser(Long userId, User newUser) {
        Optional<User> foundUser = users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst();

        if (foundUser.isPresent()) {
            User user = foundUser.get();
            if (newUser.getEmail() != null && !newUser.getEmail().trim().isEmpty()) {
                emailExists(newUser.getEmail());
                user.setEmail(newUser.getEmail());
            }
            if (newUser.getName() != null && !newUser.getName().trim().isEmpty()) {
                user.setName(newUser.getName());
            }
            return user;
        } else {
            throw new NotFoundException("User", userId);
        }
    }

    @Override
    public void deleteUser(Long userId) {
        boolean removed = users.removeIf(user -> user.getId().equals(userId));

        if (!removed) {
            throw new NotFoundException("User", userId);
        }
    }

    @Override
    public User findUser(Long userId) {
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User", userId));
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    private long getId() {
        return users.stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0L) + 1;
    }

    private void emailExists(String email) {
        boolean emailExists = users.stream()
                .anyMatch(u -> u.getEmail().equals(email));
        if (emailExists) {
            throw new IllegalArgumentException("Email already exists! - " + email);
        }
    }
}
