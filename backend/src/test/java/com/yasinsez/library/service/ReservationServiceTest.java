package com.yasinsez.library.service;

import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.Reservation;
import com.yasinsez.library.model.enums.ReservationStatus;
import com.yasinsez.library.repository.BookRepository;
import com.yasinsez.library.repository.MemberRepository;
import com.yasinsez.library.repository.ReservationRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ReservationServiceTest {

    @Inject
    ReservationService reservationService;

    @InjectMock
    ReservationRepository reservationRepository;

    @InjectMock
    MemberRepository memberRepository;

    @InjectMock
    BookRepository bookRepository;

    private Member member;
    private Book book;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);

        book = new Book();
        book.setId(1L);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setMember(member);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.PENDING);
    }

    @Test
    void createReservation_Success() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(member));
        when(bookRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(book));

        Reservation createdReservation = reservationService.createReservation(1L, 1L);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).persist(reservationCaptor.capture());

        Reservation persistedReservation = reservationCaptor.getValue();
        assertEquals(member, persistedReservation.getMember());
        assertEquals(book, persistedReservation.getBook());
        assertEquals(ReservationStatus.PENDING, persistedReservation.getStatus());
    }

    @Test
    void createReservation_MemberNotFound() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> reservationService.createReservation(1L, 1L));
    }

    @Test
    void createReservation_BookNotFound() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(member));
        when(bookRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> reservationService.createReservation(1L, 1L));
    }

    @Test
    void cancelReservation_Success() {
        when(reservationRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(reservation));
        reservationService.cancelReservation(1L);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void cancelReservation_NotFound() {
        when(reservationRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> reservationService.cancelReservation(1L));
    }

    @Test
    void getReservationsByMember_Success() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(member));
        when(reservationRepository.findByMember(any(Member.class))).thenReturn(Collections.singletonList(reservation));

        List<Reservation> reservations = reservationService.getReservationsByMember(1L);

        assertFalse(reservations.isEmpty());
        assertEquals(1, reservations.size());
        assertEquals(reservation, reservations.get(0));
    }

    @Test
    void getReservationsByMember_MemberNotFound() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> reservationService.getReservationsByMember(1L));
    }
}

