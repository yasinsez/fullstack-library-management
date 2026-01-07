package com.yasinsez.library.repository;

import com.yasinsez.library.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public User updateUserByUsername(String username, User user) {
        // This is not a direct method in Panache, so we implement it manually.
        User entity = findByUsername(username).orElse(null);
        if (entity != null) {
            entity.setFullName(user.getFullName());
            entity.setPhoneNumber(user.getPhoneNumber());
            persist(entity);
        }
        return entity;
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public List<User> findByStatus(String status) {
        return list("status", status);
    }

    public List<User> findActiveUsers() {
        return list("active", true);
    }

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public List<User> searchUsers(String searchTerm) {
        String lowerSearchTerm = "%" + searchTerm.toLowerCase() + "%";
        return list("LOWER(username) LIKE ?1 OR LOWER(email) LIKE ?1 OR LOWER(fullName) LIKE ?1", lowerSearchTerm);
    }
}
