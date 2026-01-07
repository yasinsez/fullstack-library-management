package com.yasinsez.library.service;

import com.yasinsez.library.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;

@ApplicationScoped
public class ReceiptService {
    private static final Logger logger = Logger.getLogger(ReceiptService.class.getName());

    public Receipt createReceipt(Transaction transaction) {
        Receipt receipt = new Receipt();
        receipt.setTransaction(transaction);
        receipt.setIssueDate(LocalDate.now());
        receipt.setItems(transaction.getDescription());
        receipt.setTotal(transaction.getAmount());

        return receipt;
    }

    public void emailReceipt(Receipt receipt, String email) {
        System.out.println("Emailing receipt " + receipt.getId() + " to " + email);
    }

    public void printReceipt(Receipt receipt) {
        System.out.println("Printing receipt " + receipt.getId());
    }

    public Receipt generateReturnReceipt(Loan loan, Fine fine) {
        String bookTitle = "Unknown";
        if (loan != null && loan.getBookCopy() != null && loan.getBookCopy().getBook() != null) {
            bookTitle = loan.getBookCopy().getBook().getTitle();
        }

        String description = "Book Return: " + bookTitle;
        BigDecimal amount = fine != null ? fine.getAmount() : BigDecimal.ZERO;

        if (fine != null) {
            description += " (Late Fee: $" + fine.getAmount() + ")";
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDate(LocalDate.now());
        transaction.setDescription(description);
        transaction.setMember(loan.getMember());

        Receipt receipt = new Receipt();
        receipt.setTransaction(transaction);
        receipt.setIssueDate(LocalDate.now());
        receipt.setItems(description);
        receipt.setTotal(amount);
        return receipt;
    }
}
