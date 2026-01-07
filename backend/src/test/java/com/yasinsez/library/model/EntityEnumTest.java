package com.yasinsez.library.model;

import com.yasinsez.library.model.enums.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify that all entities properly use enum types
 * and that enum values match the database schema
 */
@DisplayName("Entity Enum Integration Tests")
class EntityEnumTest {

    @Test
    @DisplayName("User entity should use UserStatus enum")
    void testUserStatusEnum() {
        User user = new User();
        
        user.setStatus(UserStatus.ACTIVE);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        
        user.setStatus(UserStatus.SUSPENDED);
        assertEquals(UserStatus.SUSPENDED, user.getStatus());
    }

    @Test
    @DisplayName("Loan entity should use LoanStatus enum")
    void testLoanStatusEnum() {
        Loan loan = new Loan();
        
        loan.setStatus(LoanStatus.ACTIVE);
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        
        loan.setStatus(LoanStatus.OVERDUE);
        assertEquals(LoanStatus.OVERDUE, loan.getStatus());
        
        loan.setStatus(LoanStatus.RETURNED);
        assertEquals(LoanStatus.RETURNED, loan.getStatus());
    }

    @Test
    @DisplayName("BookCopy entity should use CopyStatus enum")
    void testCopyStatusEnum() {
        BookCopy bookCopy = new BookCopy();
        
        bookCopy.setStatus(CopyStatus.AVAILABLE);
        assertEquals(CopyStatus.AVAILABLE, bookCopy.getStatus());
        
        bookCopy.setStatus(CopyStatus.CHECKED_OUT);
        assertEquals(CopyStatus.CHECKED_OUT, bookCopy.getStatus());
        
        bookCopy.setStatus(CopyStatus.RESERVED);
        assertEquals(CopyStatus.RESERVED, bookCopy.getStatus());
    }

    @Test
    @DisplayName("Reservation entity should use ReservationStatus enum")
    void testReservationStatusEnum() {
        Reservation reservation = new Reservation();
        
        reservation.setStatus(ReservationStatus.PENDING);
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        
        reservation.setStatus(ReservationStatus.READY_FOR_PICKUP);
        assertEquals(ReservationStatus.READY_FOR_PICKUP, reservation.getStatus());
        
        reservation.setStatus(ReservationStatus.COMPLETED);
        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
    }

    @Test
    @DisplayName("Fine entity should use FineStatus and FineReason enums")
    void testFineEnums() {
        Fine fine = new Fine();
        
        fine.setStatus(FineStatus.PENDING);
        fine.setReason(FineReason.OVERDUE);
        
        assertEquals(FineStatus.PENDING, fine.getStatus());
        assertEquals(FineReason.OVERDUE, fine.getReason());
        
        fine.setStatus(FineStatus.PAID);
        fine.setReason(FineReason.DAMAGED_ITEM);
        
        assertEquals(FineStatus.PAID, fine.getStatus());
        assertEquals(FineReason.DAMAGED_ITEM, fine.getReason());
    }

    @Test
    @DisplayName("Transaction entity should use TransactionType enum")
    void testTransactionTypeEnum() {
        Transaction transaction = new Transaction();
        
        transaction.setType(TransactionType.FINE_PAYMENT);
        assertEquals(TransactionType.FINE_PAYMENT, transaction.getType());
        
        transaction.setType(TransactionType.MEMBERSHIP_FEE);
        assertEquals(TransactionType.MEMBERSHIP_FEE, transaction.getType());
        
        transaction.setType(TransactionType.REFUND);
        assertEquals(TransactionType.REFUND, transaction.getType());
    }

    @Test
    @DisplayName("LoanHistory entity should use LoanAction enum")
    void testLoanActionEnum() {
        LoanHistory loanHistory = new LoanHistory();
        
        loanHistory.setAction(LoanAction.CHECKOUT);
        assertEquals(LoanAction.CHECKOUT, loanHistory.getAction());
        
        loanHistory.setAction(LoanAction.RETURN);
        assertEquals(LoanAction.RETURN, loanHistory.getAction());
        
        loanHistory.setAction(LoanAction.RENEWAL);
        assertEquals(LoanAction.RENEWAL, loanHistory.getAction());
    }

    @Test
    @DisplayName("Enum values should match database schema")
    void testEnumValuesMatchDatabaseSchema() {
        // Test UserStatus enum values
        assertEquals("ACTIVE", UserStatus.ACTIVE.name());
        assertEquals("INACTIVE", UserStatus.INACTIVE.name());
        assertEquals("SUSPENDED", UserStatus.SUSPENDED.name());
        
        // Test LoanStatus enum values
        assertEquals("ACTIVE", LoanStatus.ACTIVE.name());
        assertEquals("OVERDUE", LoanStatus.OVERDUE.name());
        assertEquals("RETURNED", LoanStatus.RETURNED.name());
        assertEquals("LOST", LoanStatus.LOST.name());
        
        // Test CopyStatus enum values
        assertEquals("AVAILABLE", CopyStatus.AVAILABLE.name());
        assertEquals("CHECKED_OUT", CopyStatus.CHECKED_OUT.name());
        assertEquals("IN_REPAIR", CopyStatus.IN_REPAIR.name());
        assertEquals("LOST", CopyStatus.LOST.name());
        assertEquals("RESERVED", CopyStatus.RESERVED.name());
        
        // Test ReservationStatus enum values
        assertEquals("PENDING", ReservationStatus.PENDING.name());
        assertEquals("READY_FOR_PICKUP", ReservationStatus.READY_FOR_PICKUP.name());
        assertEquals("COMPLETED", ReservationStatus.COMPLETED.name());
        assertEquals("CANCELLED", ReservationStatus.CANCELLED.name());
        
        // Test FineStatus enum values
        assertEquals("PENDING", FineStatus.PENDING.name());
        assertEquals("PAID", FineStatus.PAID.name());
        assertEquals("WAIVED", FineStatus.WAIVED.name());
        assertEquals("DISPUTED", FineStatus.DISPUTED.name());
        
        // Test FineReason enum values
        assertEquals("OVERDUE", FineReason.OVERDUE.name());
        assertEquals("DAMAGED_ITEM", FineReason.DAMAGED_ITEM.name());
        assertEquals("LOST_ITEM", FineReason.LOST_ITEM.name());
        assertEquals("ADMINISTRATIVE", FineReason.ADMINISTRATIVE.name());
        
        // Test TransactionType enum values
        assertEquals("FINE_PAYMENT", TransactionType.FINE_PAYMENT.name());
        assertEquals("MEMBERSHIP_FEE", TransactionType.MEMBERSHIP_FEE.name());
        assertEquals("LOST_ITEM_FEE", TransactionType.LOST_ITEM_FEE.name());
        assertEquals("RESERVATION_FEE", TransactionType.RESERVATION_FEE.name());
        assertEquals("REFUND", TransactionType.REFUND.name());
        
        // Test LoanAction enum values
        assertEquals("CHECKOUT", LoanAction.CHECKOUT.name());
        assertEquals("RETURN", LoanAction.RETURN.name());
        assertEquals("RENEWAL", LoanAction.RENEWAL.name());
        assertEquals("OVERDUE_NOTICE", LoanAction.OVERDUE_NOTICE.name());
    }
} 