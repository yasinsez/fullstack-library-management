package com.yasinsez.library.repository;

import com.yasinsez.library.model.Loan;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.enums.LoanStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class LoanRepository implements PanacheRepository<Loan> {

    public List<Loan> findByMember(Member member) {
        return list("member", member);
    }

    public List<Loan> findActiveLoansByMember(Member member) {
        return list("member = ?1 and status = ?2", member, LoanStatus.ACTIVE);
    }

    public List<Loan> findReturnedLoansByMember(Member member) {
        return list("member = ?1 and status = ?2", member, LoanStatus.RETURNED);
    }

    public long countActiveLoansByMember(Member member) {
        return count("member = ?1 and status = ?2", member, LoanStatus.ACTIVE);
    }

    public boolean existsActiveLoanForMemberAndBook(Member member, Long bookId) {
        return count("member = ?1 and bookCopy.book.id = ?2 and status = ?3", member, bookId, LoanStatus.ACTIVE) > 0;
    }

    public List<Loan> findOverdueLoans() {
        return list("dueDate < ?1 and status = ?2", LocalDate.now(), LoanStatus.ACTIVE);
    }

    public List<Loan> findByCheckoutDateBetween(LocalDate startDate, LocalDate endDate) {
        return list("checkoutDate >= ?1 and checkoutDate <= ?2", startDate, endDate);
    }

    public List<Loan> findByStatus(LoanStatus status) {
        return list("status", status);
    }
}
