package com.yasinsez.library.service;

import com.yasinsez.library.model.Fine;
import com.yasinsez.library.model.Loan;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.enums.FineReason;
import com.yasinsez.library.model.enums.FineStatus;
import com.yasinsez.library.repository.FineRepository;
import com.yasinsez.library.repository.MemberRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class FineServiceTest {

    @Inject
    FineService fineService;

    @InjectMock
    FineRepository fineRepository;

    @InjectMock
    MemberRepository memberRepository;

    private Fine fine;
    private Member member;
    private Loan loan;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setFineBalance(new BigDecimal("25.00"));

        loan = new Loan();
        loan.setId(1L);
        loan.setMember(member);

        fine = new Fine();
        fine.setId(1L);
        fine.setMember(member);
        fine.setLoan(loan);
        fine.setAmount(new BigDecimal("25.00"));
        fine.setStatus(FineStatus.PENDING);
    }

    @Test
    void createFine_Success() {
        fineService.createFine(fine);
        ArgumentCaptor<Fine> fineCaptor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).persist(fineCaptor.capture());
        assertEquals(fine, fineCaptor.getValue());
    }

    @Test
    void payFine_Success() {
        when(fineRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(fine));
        Fine paidFine = fineService.payFine(1L);

        assertEquals(FineStatus.PAID, paidFine.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(member.getFineBalance()));
    }

    @Test
    void payFine_NotFound() {
        when(fineRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> fineService.payFine(1L));
    }

    @Test
    void getFinesForMember_Success() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(member));
        when(fineRepository.findByMember(any(Member.class))).thenReturn(Collections.singletonList(fine));
        List<Fine> fines = fineService.getFinesForMember(1L);
        assertFalse(fines.isEmpty());
        assertEquals(1, fines.size());
        assertEquals(fine, fines.get(0));
    }

    @Test
    void getFinesForMember_MemberNotFound() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> fineService.getFinesForMember(1L));
    }

    @Test
    void createOverdueFine_Success() {
        Fine overdueFine = fineService.createOverdueFine(loan);

        ArgumentCaptor<Fine> fineCaptor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).persist(fineCaptor.capture());

        Fine persistedFine = fineCaptor.getValue();
        assertEquals(member, persistedFine.getMember());
        assertEquals(loan, persistedFine.getLoan());
        assertEquals(new BigDecimal("25.00"), persistedFine.getAmount());
        assertEquals(FineReason.OVERDUE, persistedFine.getReason());
        assertEquals(FineStatus.PENDING, persistedFine.getStatus());
    }
}

