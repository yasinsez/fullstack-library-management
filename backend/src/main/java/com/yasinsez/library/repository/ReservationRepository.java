package com.yasinsez.library.repository;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.Reservation;
import com.yasinsez.library.model.enums.ReservationStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ReservationRepository implements PanacheRepository<Reservation> {

    public List<Reservation> findByMember(Member member) {
        return list("member", member);
    }

    public List<Reservation> findPendingReservationsByBook(Book book) {
        return list("book = ?1 and status = ?2", book, ReservationStatus.PENDING);
    }

    public long countActiveReservationsByMember(Member member) {
        return count("member = ?1 and status in (?2, ?3)", member, ReservationStatus.PENDING,
                ReservationStatus.READY_FOR_PICKUP);
    }

    public boolean existsActiveReservationForMemberAndBook(Member member, Long bookId) {
        return count("member = ?1 and book.id = ?2 and status in (?3, ?4)", member, bookId,
                ReservationStatus.PENDING, ReservationStatus.READY_FOR_PICKUP) > 0;
    }
}
