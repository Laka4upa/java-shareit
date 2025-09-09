package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(counter.getAndIncrement());
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
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return user;
        }
        throw new NoSuchElementException("User not found with id: " + user.getId());
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
}