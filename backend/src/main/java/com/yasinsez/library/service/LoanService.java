package com.yasinsez.library.service;

import com.yasinsez.library.mapper.LoanMapper;
import com.yasinsez.library.dto.LoanResponseDTO;
import com.yasinsez.library.model.BookCopy;
import com.yasinsez.library.model.Book;
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
import com.yasinsez.library.repository.ReservationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);
    private static final int LOAN_PERIOD_DAYS = 14;
    private static final int MAX_LOANS_PER_MEMBER = 3;
    private static final BigDecimal LATE_FEE_PER_DAY = new BigDecimal("0.25");

    @Inject
    LoanRepository loanRepository;

    @Inject
    BookCopyRepository bookCopyRepository;

    @Inject
    FineRepository fineRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    ReservationRepository reservationRepository;

    @Transactional
    public LoanResponseDTO borrowBook(Long bookId, String username) {
        log.info("Member {} attempting to borrow book {}", username, bookId);

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Member not found with username: " + username));

        return checkoutBook(bookId, member.getId());
    }

    @Transactional
    public LoanResponseDTO checkoutBook(Long bookId, Long userId) {
        log.info("Attempting to check out book {} for user {}", bookId, userId);

        Member member = memberRepository.findByIdOptional(userId)
                .orElseThrow(() -> new NotFoundException("Member not found for the given user"));

        if (member.getUser() == null) {
            log.error("Data inconsistency: Member record found for ID {}, but it has no associated User.", userId);
            throw new InternalServerErrorException("Could not process loan due to a data inconsistency issue.");
        }

        log.info("Found member: {}", member.getUser().getUsername());

        if (loanRepository.countActiveLoansByMember(member) >= (long) MAX_LOANS_PER_MEMBER) {
            log.warn("Member {} has reached the maximum number of active loans.", userId);
            throw new BadRequestException("Member has reached the maximum number of active loans.");
        }

        if (loanRepository.existsActiveLoanForMemberAndBook(member, bookId)) {
            log.warn("Member {} already has an active loan for book {}.", userId, bookId);
            throw new BadRequestException("Member already has an active loan for this book.");
        }

        if (member.getFineBalance() != null && member.getFineBalance().compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Member {} has outstanding fines.", userId);
            throw new BadRequestException("Member has outstanding fines.");
        }

        BookCopy bookCopy = bookCopyRepository.findAvailableByBookId(bookId)
                .orElseThrow(() -> new NotFoundException("No available copies for this book."));
        log.info("Found available book copy: {}", bookCopy.getId());

        bookCopy.setStatus(CopyStatus.CHECKED_OUT);

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBookCopy(bookCopy);
        loan.setCheckoutDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(LOAN_PERIOD_DAYS));
        loan.setStatus(LoanStatus.ACTIVE);
        loanRepository.persist(loan);

        log.info(
                "{} borrowed '{}'",
                member.getUser().getFullName(),
                bookCopy.getBook().getTitle());
        log.info("Book checked out successfully. Loan ID: {}", loan.getId());
        // After successful checkout, clean up any active reservations for this member/book
        try {
            Book book = bookCopy.getBook();
            Member m = member;
            // Delete READY or PENDING reservations for this member and this book
            java.util.List<com.yasinsez.library.model.Reservation> toDelete = reservationRepository.list(
                    "member = ?1 and book = ?2 and status in (?3, ?4)",
                    m, book,
                    com.yasinsez.library.model.enums.ReservationStatus.PENDING,
                    com.yasinsez.library.model.enums.ReservationStatus.READY_FOR_PICKUP);
            for (com.yasinsez.library.model.Reservation r : toDelete) {
                reservationRepository.delete(r);
            }
            // Renumber pending queue after deletions
            int next = 1;
            for (com.yasinsez.library.model.Reservation r : reservationRepository.findPendingReservationsByBook(book)) {
                r.setPriorityNumber(next++);
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup reservations after checkout: {}", e.getMessage());
        }

        return LoanMapper.toResponseDTO(loan);
    }

    public List<LoanResponseDTO> getCurrentLoansByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Member not found with username: " + username));
        return loanRepository.findActiveLoansByMember(member).stream()
                .map(LoanMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<LoanResponseDTO> getLoanHistoryByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Member not found with username: " + username));
        return loanRepository.findReturnedLoansByMember(member).stream()
                .map(LoanMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void returnBookForUser(Long loanId, String username, boolean isStaff) {
        Loan loan = loanRepository.findByIdOptional(loanId)
                .orElseThrow(() -> new NotFoundException("Loan not found"));

        if (!isStaff) {
            if (loan.getMember() == null || loan.getMember().getUser() == null ||
                    !loan.getMember().getUser().getUsername().equals(username)) {
                throw new ForbiddenException("You are not authorized to return this loan.");
            }
        }

        returnBook(loanId);
    }

    @Transactional
    public void returnBook(Long loanId) {
        log.info("Returning book for loan {}", loanId);
        Loan loan = loanRepository.findByIdOptional(loanId)
                .orElseThrow(() -> new NotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BadRequestException("Loan is not active.");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        BookCopy bookCopy = loan.getBookCopy();
        bookCopy.setStatus(CopyStatus.AVAILABLE);

        if (loan.getDueDate().isBefore(LocalDate.now())) {
            createFineForOverdueLoan(loan);
        }

        // reservation queue processing: mark next pending as READY_FOR_PICKUP (no
        // auto-checkout)
        try {
            Book book = bookCopy.getBook();
            var pending = reservationRepository.findPendingReservationsByBook(book);
            if (!pending.isEmpty()) {
                // sort by priority
                java.util.List<com.yasinsez.library.model.Reservation> sorted = new java.util.ArrayList<>(pending);
                sorted.sort(java.util.Comparator
                        .comparingInt(r -> r.getPriorityNumber() == null ? Integer.MAX_VALUE : r.getPriorityNumber()));
                // mark first as READY_FOR_PICKUP
                var first = sorted.get(0);
                first.setStatus(com.yasinsez.library.model.enums.ReservationStatus.READY_FOR_PICKUP);
                // renumber remaining pending to fill gaps
                int nextPriority = 1;
                for (com.yasinsez.library.model.Reservation r : reservationRepository
                        .findPendingReservationsByBook(book)) {
                    r.setPriorityNumber(nextPriority++);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to process reservation queue on return: {}", e.getMessage());
        }
    }

    private void createFineForOverdueLoan(Loan loan) {
        long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
        if (overdueDays > 0) {
            BigDecimal fineAmount = LATE_FEE_PER_DAY.multiply(new BigDecimal(overdueDays));

            Fine fine = new Fine();
            fine.setMember(loan.getMember());
            fine.setLoan(loan);
            fine.setAmount(fineAmount);
            fine.setReason(FineReason.OVERDUE);
            fine.setIssueDate(LocalDate.now());
            fine.setStatus(FineStatus.PENDING);
            fineRepository.persist(fine);

            Member member = loan.getMember();
            BigDecimal currentBalance = member.getFineBalance() == null ? BigDecimal.ZERO : member.getFineBalance();
            member.setFineBalance(currentBalance.add(fineAmount));
            log.info("Created a fine of {} for member {}", fineAmount, member.getId());
        }
    }

    public List<Loan> getLoansForMember(Long memberId) {
        Member member = memberRepository.findByIdOptional(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        return loanRepository.findByMember(member);
    }

    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans();
    }

    public List<Loan> getLoansByStatus(LoanStatus status) {
        return loanRepository.findByStatus(status);
    }

    @Transactional
    public LoanResponseDTO updateLoanDueDate(Long loanId, LocalDate newDueDate) {
        Loan loan = loanRepository.findByIdOptional(loanId)
                .orElseThrow(() -> new NotFoundException("Loan not found"));
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BadRequestException("Only active loans can be updated.");
        }
        if (newDueDate.isBefore(loan.getCheckoutDate())) {
            throw new BadRequestException("Due date cannot be before checkout date.");
        }
        loan.setDueDate(newDueDate);
        return LoanMapper.toResponseDTO(loan);
    }
}
