package com.yasinsez.library.service;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.dto.LoanResponseDTO;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.Reservation;
import com.yasinsez.library.model.enums.ReservationStatus;
import com.yasinsez.library.repository.BookRepository;
import com.yasinsez.library.repository.MemberRepository;
import com.yasinsez.library.repository.ReservationRepository;
import com.yasinsez.library.repository.BookCopyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    BookRepository bookRepository;

    @Inject
    LoanService loanService;

    @Inject
    BookCopyRepository bookCopyRepository;

    @Transactional
    public Reservation createReservation(Long memberId, Long bookId) {
        log.info("Creating reservation for member {} and book {}", memberId, bookId);

        Member member = memberRepository.findByIdOptional(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        Book book = bookRepository.findByIdOptional(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        // Enforce limits
        if (reservationRepository.existsActiveReservationForMemberAndBook(member, bookId)) {
            throw new BadRequestException("You already have an active reservation for this book.");
        }
        if (reservationRepository.countActiveReservationsByMember(member) >= 3) {
            throw new BadRequestException("You have reached the maximum number of active reservations (3).");
        }

        Reservation reservation = new Reservation();
        reservation.setMember(member);
        reservation.setBook(book);
        reservation.setReservationTime(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);
        // assign queue position: 1 + number of existing pending reservations for this
        // book
        int queueSize = reservationRepository.findPendingReservationsByBook(book).size();
        reservation.setPriorityNumber(queueSize + 1);

        // If this is the first reservation and a copy is currently available, mark
        // READY_FOR_PICKUP
        // and set priority to 1 so the first in queue is ready automatically
        boolean isFirstInQueue = queueSize == 0;
        boolean hasAvailableCopy = bookCopyRepository.findAvailableByBookId(bookId).isPresent();
        if (isFirstInQueue && hasAvailableCopy) {
            // Ensure there isn't already a READY reservation for this book
            boolean hasReady = !reservationRepository
                    .list("book = ?1 and status = ?2", book, ReservationStatus.READY_FOR_PICKUP)
                    .isEmpty();
            if (!hasReady) {
                reservation.setStatus(ReservationStatus.READY_FOR_PICKUP);
                reservation.setPriorityNumber(1);
            }
        }
        reservationRepository.persist(reservation);

        log.info("Reservation created successfully with ID {}", reservation.getId());
        return reservation;
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        log.info("Cancelling reservation {} (admin/librarian)", reservationId);
        Reservation reservation = reservationRepository.findByIdOptional(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        Book book = reservation.getBook();
        boolean wasReady = reservation.getStatus() == ReservationStatus.READY_FOR_PICKUP;
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setPriorityNumber(null);

        // If the cancelled reservation was READY_FOR_PICKUP, promote the next pending
        // one (if any)
        if (wasReady) {
            promoteNextPendingToReady(book);
        }
        // Always renumber pending queue to fill gaps
        renumberPendingQueue(book);
    }

    public List<Reservation> getReservationsByMember(Long memberId) {
        Member member = memberRepository.findByIdOptional(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        return reservationRepository.findByMember(member);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.listAll();
    }

    @Transactional
    public void updateReservationStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findByIdOptional(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
        reservation.setStatus(status);
    }

    @Transactional
    public void cancelReservationByMember(Long reservationId, String username) {
        log.info("Member {} cancelling reservation {}", username, reservationId);
        Reservation reservation = reservationRepository.findByIdOptional(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        if (reservation.getMember() == null || reservation.getMember().getUser() == null
                || reservation.getMember().getUser().getUsername() == null
                || !reservation.getMember().getUser().getUsername().equals(username)) {
            throw new BadRequestException("You can only cancel your own reservations.");
        }

        Book book = reservation.getBook();
        boolean wasReady = reservation.getStatus() == ReservationStatus.READY_FOR_PICKUP;
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setPriorityNumber(null);

        if (wasReady) {
            promoteNextPendingToReady(book);
        }
        renumberPendingQueue(book);
    }

    @Transactional
    public void hardDeleteReservation(Long reservationId) {
        log.info("Hard deleting reservation {}", reservationId);
        Reservation reservation = reservationRepository.findByIdOptional(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        Book book = reservation.getBook();
        boolean wasReady = reservation.getStatus() == ReservationStatus.READY_FOR_PICKUP;

        reservationRepository.delete(reservation);

        if (wasReady) {
            promoteNextPendingToReady(book);
        }
        renumberPendingQueue(book);
    }

    @Transactional
    public void updateReservationPriority(Long reservationId, int newPriority) {
        Reservation target = reservationRepository.findByIdOptional(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        if (target.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Only PENDING reservations can be reprioritized.");
        }

        Book book = target.getBook();
        java.util.List<Reservation> pending = new java.util.ArrayList<>(
                reservationRepository.findPendingReservationsByBook(book));
        // Sort by existing priority number (nulls last)
        pending.sort(java.util.Comparator
                .comparingInt(r -> r.getPriorityNumber() == null ? Integer.MAX_VALUE : r.getPriorityNumber()));

        // Remove the target from the list if present
        pending.removeIf(r -> r.getId().equals(target.getId()));

        int clamped = Math.max(1, Math.min(newPriority, pending.size() + 1));
        // Insert target at the desired position (1-indexed)
        pending.add(clamped - 1, target);

        // Renumber sequentially starting at 1
        int idx = 1;
        for (Reservation r : pending) {
            r.setPriorityNumber(idx++);
        }
    }

    @Transactional
    public LoanResponseDTO borrowReadyReservation(Long reservationId, String username) {
        Reservation reservation = reservationRepository.findByIdOptional(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.READY_FOR_PICKUP) {
            throw new BadRequestException("Reservation is not ready for pickup.");
        }
        if (reservation.getMember() == null || reservation.getMember().getUser() == null
                || reservation.getMember().getUser().getUsername() == null
                || !reservation.getMember().getUser().getUsername().equals(username)) {
            throw new BadRequestException("You can only borrow your own ready reservations.");
        }

        // Perform checkout for this reservation's book and member
        LoanResponseDTO loan = loanService.checkoutBook(reservation.getBook().getId(), reservation.getMember().getId());

        // Remove reservation so it does not restart the queue after borrowing
        Book book = reservation.getBook();
        reservationRepository.delete(reservation);
        renumberPendingQueue(book);

        return loan;
    }

    private void promoteNextPendingToReady(Book book) {
        java.util.List<Reservation> pending = new java.util.ArrayList<>(
                reservationRepository.findPendingReservationsByBook(book));
        if (pending.isEmpty())
            return;
        pending.sort(java.util.Comparator
                .comparingInt(r -> r.getPriorityNumber() == null ? Integer.MAX_VALUE : r.getPriorityNumber()));
        Reservation next = pending.get(0);
        next.setStatus(ReservationStatus.READY_FOR_PICKUP);
    }

    private void renumberPendingQueue(Book book) {
        java.util.List<Reservation> pending = new java.util.ArrayList<>(
                reservationRepository.findPendingReservationsByBook(book));
        pending.sort(java.util.Comparator
                .comparingInt(r -> r.getPriorityNumber() == null ? Integer.MAX_VALUE : r.getPriorityNumber()));
        int i = 1;
        for (Reservation r : pending) {
            r.setPriorityNumber(i++);
        }
    }
}
