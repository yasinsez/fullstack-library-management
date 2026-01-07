package com.yasinsez.library.repository;

import com.yasinsez.library.model.Fine;
import com.yasinsez.library.model.Loan;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.enums.FineStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FineRepository implements PanacheRepository<Fine> {

    public List<Fine> findByMember(Member member) {
        return list("member", member);
    }

    public List<Fine> findByMemberOrderByIssueDateDesc(Member member) {
        return find("member", Sort.by("issueDate").descending().and("id", Sort.Direction.Descending), member).list();
    }

    public List<Fine> listAllOrderByIssueDateDesc() {
        return listAll(Sort.by("issueDate").descending().and("id", Sort.Direction.Descending));
    }

    public Optional<Fine> findByLoan(Loan loan) {
        return find("loan", loan).firstResultOptional();
    }

    public List<Fine> findPendingByMember(Member member) {
        return list("member = ?1 AND status = ?2", member, FineStatus.PENDING);
    }

    public List<Fine> findOverdueFines() {
        return list("dueDate < ?1 AND status = ?2", LocalDate.now(), FineStatus.PENDING);
    }

    public List<Fine> findByIssueDateBetween(LocalDate startDate, LocalDate endDate) {
        return list("issueDate >= ?1 and issueDate <= ?2", startDate, endDate);
    }
}
