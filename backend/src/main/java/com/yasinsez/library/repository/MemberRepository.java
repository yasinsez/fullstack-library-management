package com.yasinsez.library.repository;

import com.yasinsez.library.model.Member;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class MemberRepository implements PanacheRepository<Member> {

    public Optional<Member> findByUsername(String username) {
        return find("SELECT m FROM Member m LEFT JOIN FETCH m.user WHERE m.user.username = ?1", username)
                .firstResultOptional();
    }
}
