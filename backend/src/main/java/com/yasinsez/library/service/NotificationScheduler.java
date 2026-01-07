package com.yasinsez.library.service;

import com.yasinsez.library.model.Loan;
import com.yasinsez.library.model.Reservation;
import com.yasinsez.library.model.enums.ReservationStatus;
import com.yasinsez.library.repository.ReservationRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    @Inject
    LoanService loanService;

    @Inject
    NotificationService notificationService;

    @Inject
    ReservationRepository reservationRepository;

    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9am
    void sendOverdueLoanNotices() {
        log.info("Running scheduled job: Send Overdue Loan Notices");
        List<Loan> overdueLoans = loanService.getOverdueLoans();
        for (Loan loan : overdueLoans) {
            notificationService.sendOverdueNotice(loan);
        }
    }

    @Scheduled(cron = "0 */30 * * * ?") // Every 30 minutes
    void sendReservationAvailableNotices() {
        log.info("Running scheduled job: Send Reservation Available Notices");
        List<Reservation> reservations = reservationRepository.list("status", ReservationStatus.READY_FOR_PICKUP);
        for (Reservation reservation : reservations) {
            notificationService.sendReservationAvailableNotification(reservation);
        }
    }
}
