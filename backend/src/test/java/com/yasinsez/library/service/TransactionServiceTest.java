package com.yasinsez.library.service;

import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.Transaction;
import com.yasinsez.library.model.enums.TransactionType;
import com.yasinsez.library.repository.TransactionRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import java.util.Optional;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class TransactionServiceTest {

    @Inject
    TransactionService transactionService;

    @InjectMock
    TransactionRepository transactionRepository;

    private Transaction transaction;
    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setMember(member);
        transaction.setAmount(new BigDecimal("10.00"));
        transaction.setType(TransactionType.FINE_PAYMENT);
        transaction.setDate(LocalDate.now());
    }

    @Test
    void createTransaction_Success() {
        transactionService.createTransaction(transaction);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).persist(transactionCaptor.capture());
        assertEquals(transaction, transactionCaptor.getValue());
    }

    @Test
    void getTransactionById_Success() {
        when(transactionRepository.findById(anyLong())).thenReturn(transaction);
        Transaction foundTransaction = transactionService.getTransactionById(1L);
        assertNotNull(foundTransaction);
        assertEquals(transaction.getId(), foundTransaction.getId());
    }

    @Test
    void getTransactionsByMember_Success() {
        when(transactionRepository.list("member.id", 1L)).thenReturn(Collections.singletonList(transaction));
        List<Transaction> transactions = transactionService.getTransactionsByMember(1L);
        assertEquals(1, transactions.size());
        assertEquals(transaction, transactions.get(0));
    }


}

