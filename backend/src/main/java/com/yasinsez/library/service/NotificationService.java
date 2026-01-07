package com.yasinsez.library.service;

import com.yasinsez.library.model.Loan;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.model.Reservation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Inject
    EmailService emailService;

    public void sendCheckoutNotification(Member member, Loan loan) {
        String subject = "Book Checkout Confirmation";
        String body = String.format("Dear %s,\n\nYou have successfully checked out the book '%s'.\nDue Date: %s",
                member.getUser().getFullName(), loan.getBookCopy().getBook().getTitle(), loan.getDueDate());
        log.info("Sending checkout notification to {}", member.getUser().getEmail());
        emailService.sendEmail(member.getUser().getEmail(), subject, body);
    }

    public void sendReturnNotification(Member member, Loan loan) {
        String subject = "Book Return Confirmation";
        String body = String.format("Dear %s,\n\nYou have successfully returned the book '%s'.\nThank you!",
                member.getUser().getFullName(), loan.getBookCopy().getBook().getTitle());
        log.info("Sending return notification to {}", member.getUser().getEmail());
        emailService.sendEmail(member.getUser().getEmail(), subject, body);
    }

    public void sendOverdueNotice(Loan loan) {
        String subject = "Overdue Book Notice";
        String body = String.format("Dear %s,\n\nThis is a reminder that the book '%s' is overdue. Please return it as soon as possible to avoid further fines.",
                loan.getMember().getUser().getFullName(), loan.getBookCopy().getBook().getTitle());
        log.info("Sending overdue notice to {}", loan.getMember().getUser().getEmail());
        emailService.sendEmail(loan.getMember().getUser().getEmail(), subject, body);
    }

    public void sendReservationAvailableNotification(Reservation reservation) {
        String subject = "Book Reservation Available";
        String body = String.format("Dear %s,\n\nThe book '%s' that you reserved is now available for pickup.",
                reservation.getMember().getUser().getFullName(), reservation.getBook().getTitle());
        log.info("Sending reservation available notification to {}", reservation.getMember().getUser().getEmail());
        emailService.sendEmail(reservation.getMember().getUser().getEmail(), subject, body);
    }
}
