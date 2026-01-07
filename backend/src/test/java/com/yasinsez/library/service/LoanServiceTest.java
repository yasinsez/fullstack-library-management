package com.yasinsez.library.service;

import com.yasinsez.library.model.BookCopy;
import com.yasinsez.library.model.Fine;
import com.yasinsez.library.model.Loan;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.enums.CopyStatus;
import com.yasinsez.library.model.enums.FineReason;
import com.yasinsez.library.model.enums.FineStatus;
import com.yasinsez.library.model.enums.LoanStatus;
import com.yasinsez.library.repository.BookCopyRepository;
import com.yasinsez.library.repository.FineRepository;
import com.yasinsez.library.repository.LoanRepository;
import com.yasinsez.library.repository.MemberRepository;
import io.quarkus.test.junit.QuarkusTest;
import com.yasinsez.library.model.User;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.yasinsez.library.dto.LoanResponseDTO;
import com.yasinsez.library.model.Book;
import com.yasinsez.library.model.Publisher;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

@QuarkusTest
public class LoanServiceTest {

    @Inject
    LoanService loanService;

    @InjectMock
    LoanRepository loanRepository;

    @InjectMock
    BookCopyRepository bookCopyRepository;

    @InjectMock
    FineRepository fineRepository;

    @InjectMock
    MemberRepository memberRepository;

    private Member member;
    private BookCopy bookCopy;
    private Loan loan;
    private User user;
    private Book book;
    private Publisher publisher;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        member = new Member();
        member.setId(1L);
        member.setFineBalance(BigDecimal.ZERO);
        member.setUser(user);

        publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Test Publisher");

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setPublisher(publisher);

        bookCopy = new BookCopy();
        bookCopy.setId(1L);
        bookCopy.setStatus(CopyStatus.AVAILABLE);
        bookCopy.setBook(book);

        loan = new Loan();
        loan.setId(1L);
        loan.setMember(member);
        loan.setBookCopy(bookCopy);
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);
    }

    @Test
    void checkoutBook_Success() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(member));
        when(loanRepository.countActiveLoansByMember(any(Member.class))).thenReturn(0L);
        when(bookCopyRepository.findAvailableByBookId(anyLong())).thenReturn(Optional.of(bookCopy));

        LoanResponseDTO result = loanService.checkoutBook(1L, 1L);

        assertNotNull(result);
        assertEquals(member.getId(), result.member().id());
        assertEquals(bookCopy.getBook().getId(), result.book().id());
        assertEquals(LoanStatus.ACTIVE, result.status());
        assertEquals(CopyStatus.CHECKED_OUT, bookCopy.getStatus());

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).persist(loanCaptor.capture());
        assertEquals(member, loanCaptor.getValue().getMember());
    }

    @Test
    void checkoutBook_MemberNotFound() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> loanService.checkoutBook(1L, 1L));
    }

    @Test
    void checkoutBook_MaxLoansReached() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(member));
        when(loanRepository.countActiveLoansByMember(any(Member.class))).thenReturn(10L);
        assertThrows(BadRequestException.class, () -> loanService.checkoutBook(1L, 1L));
    }

    @Test
    void checkoutBook_HasOutstandingFines() {
        member.setFineBalance(new BigDecimal("10.00"));
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(member));
        assertThrows(BadRequestException.class, () -> loanService.checkoutBook(1L, 1L));
    }

    @Test
    void checkoutBook_NoAvailableCopies() {
        when(memberRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(member));
        when(loanRepository.countActiveLoansByMember(any(Member.class))).thenReturn(0L);
        when(bookCopyRepository.findAvailableByBookId(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> loanService.checkoutBook(1L, 1L));
    }

    @Test
    void returnBook_Success() {
        when(loanRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(loan));
        loanService.returnBook(1L);

        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        assertEquals(CopyStatus.AVAILABLE, bookCopy.getStatus());
    }

    @Test
    void returnBook_LoanNotFound() {
        when(loanRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> loanService.returnBook(1L));
    }

    @Test
    void returnBook_LoanNotActive() {
        loan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(loan));
        assertThrows(BadRequestException.class, () -> loanService.returnBook(1L));
    }

    @Test
    void returnBook_Overdue() {
        loan.setDueDate(LocalDate.now().minusDays(1));
        when(loanRepository.findByIdOptional(anyLong())).thenReturn(Optional.of(loan));

        loanService.returnBook(1L);

        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        assertEquals(CopyStatus.AVAILABLE, bookCopy.getStatus());

        ArgumentCaptor<Fine> fineCaptor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).persist(fineCaptor.capture());

        Fine fine = fineCaptor.getValue();
        assertEquals(member, fine.getMember());
        assertEquals(loan, fine.getLoan());
        assertEquals(new BigDecimal("0.25"), fine.getAmount());
        assertEquals(FineReason.OVERDUE, fine.getReason());
        assertEquals(FineStatus.PENDING, fine.getStatus());
    }
}
