package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ValidationException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);
    private final Set<String> emails = new HashSet<>();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(counter.getAndIncrement());
        }
        String emailToCheck = user.getEmail() != null ?
                user.getEmail().trim().toLowerCase() : null;
        if (emailToCheck != null && emails.contains(emailToCheck)) {
            throw new ValidationException("Email уже существует: " + user.getEmail());
        }
        if (emailToCheck != null) {
            emails.add(emailToCheck);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User update(User user) {
        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            throw new NoSuchElementException("Пользователь не найден с id: " + user.getId());
        }
        String newEmail = user.getEmail() != null ?
                user.getEmail().trim().toLowerCase() : null;
        String oldEmail = existingUser.getEmail() != null ?
                existingUser.getEmail().trim().toLowerCase() : null;
        if (newEmail != null && !newEmail.equals(oldEmail)) {
            if (emails.contains(newEmail)) {
                throw new ValidationException("Email уже существует: " + user.getEmail());
            }
        }
        if (oldEmail != null) {
            emails.remove(oldEmail);
        }
        if (newEmail != null) {
            emails.add(newEmail);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        User user = users.get(id);
        if (user != null && user.getEmail() != null) {
            emails.remove(user.getEmail().trim().toLowerCase());
        }
        users.remove(id);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long excludedId) {
        if (email == null) return false;
        String normalizedEmail = email.trim().toLowerCase();
        return users.values().stream()
                .filter(user -> !user.getId().equals(excludedId))
                .anyMatch(user -> normalizedEmail.equals(user.getEmail().trim().toLowerCase()));
    }
}