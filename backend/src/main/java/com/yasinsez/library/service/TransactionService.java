package com.yasinsez.library.service;

import com.yasinsez.library.model.*;
import com.yasinsez.library.repository.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        transactionRepository.persist(transaction);
        return transaction;
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactionsByMember(Long memberId) {
        return transactionRepository.list("member.id", memberId);
    }

    public List<Transaction> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.find("date >= ?1 and date <= ?2", startDate, endDate).list();
    }
}
