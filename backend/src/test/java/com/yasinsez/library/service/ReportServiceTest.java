package com.yasinsez.library.service;

import com.yasinsez.library.model.*;
import com.yasinsez.library.model.enums.FineStatus;
import com.yasinsez.library.model.enums.LoanStatus;
import com.yasinsez.library.repository.FineRepository;
import com.yasinsez.library.repository.LoanRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ReportServiceTest {

    @Inject
    ReportService reportService;

    @InjectMock
    LoanRepository loanRepository;

    @InjectMock
    FineRepository fineRepository;

    @InjectMock
    FineService fineService;

    private Loan loan1, loan2;
    private Fine fine1, fine2;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFullName("Test User");

        Member member = new Member();
        member.setUser(user);

        Book book1 = new Book();
        book1.setTitle("Book 1");
        Book book2 = new Book();
        book2.setTitle("Book 2");

        BookCopy bookCopy1 = new BookCopy();
        bookCopy1.setBook(book1);
        BookCopy bookCopy2 = new BookCopy();
        bookCopy2.setBook(book2);

        loan1 = new Loan();
        loan1.setStatus(LoanStatus.ACTIVE);
        loan1.setBookCopy(bookCopy1);
        loan1.setMember(member);
        loan1.setCheckoutDate(LocalDate.now());
        loan1.setDueDate(LocalDate.now().plusDays(14));

        loan2 = new Loan();
        loan2.setStatus(LoanStatus.RETURNED);
        loan2.setBookCopy(bookCopy2);
        loan2.setMember(member);
        loan2.setCheckoutDate(LocalDate.now());
        loan2.setDueDate(LocalDate.now().plusDays(14));

        fine1 = new Fine();
        fine1.setStatus(FineStatus.PAID);
        fine1.setAmount(new BigDecimal("10.00"));

        fine2 = new Fine();
        fine2.setStatus(FineStatus.PENDING);
        fine2.setAmount(new BigDecimal("5.00"));
    }

    @Test
    void generateMonthlyReport_Success() {
        when(loanRepository.findByCheckoutDateBetween(any(), any())).thenReturn(Arrays.asList(loan1, loan2));

        ReportService.MonthlyReport report = reportService.generateMonthlyReport(LocalDate.now(), LocalDate.now());

        assertEquals(2, report.getTotalLoans());
        assertEquals(1, report.getActiveLoans());
        assertEquals(1, report.getReturnedLoans());
        assertEquals(0, report.getOverdueLoans());
    }

    @Test
    void generateOverdueReport_Success() {
        loan1.setStatus(LoanStatus.ACTIVE);
        loan1.setDueDate(LocalDate.now().minusDays(1));
        when(loanRepository.findOverdueLoans()).thenReturn(Collections.singletonList(loan1));
        when(fineService.createOverdueFine(any(Loan.class))).thenReturn(new Fine(null, null, loan1, new BigDecimal("0.25"), null, null, null, FineStatus.PENDING, null, null));

        ReportService.OverdueReport report = reportService.generateOverdueReport();

        assertEquals(1, report.getTotalOverdueLoans());
        assertEquals(0.25, report.getTotalFinesOwed());
    }

    @Test
    void generateFineReport_Success() {
        when(fineRepository.findByIssueDateBetween(any(), any())).thenReturn(Arrays.asList(fine1, fine2));

        ReportService.FineReport report = reportService.generateFineReport(LocalDate.now(), LocalDate.now());

        assertEquals(2, report.getTotalFinesIssued());
        assertEquals(15.00, report.getTotalFineAmount());
        assertEquals(1, report.getFinesPaid());
        assertEquals(1, report.getFinesPending());
        assertEquals(50.0, report.getCollectionRate());
    }
}

