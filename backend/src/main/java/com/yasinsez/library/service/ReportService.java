package com.yasinsez.library.service;

import com.yasinsez.library.model.*;
import com.yasinsez.library.repository.FineRepository;
import com.yasinsez.library.repository.LoanRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReportService {

    @Inject
    LoanRepository loanRepository;

    @Inject
    FineRepository fineRepository;

    @Inject
    FineService fineService;

    public MonthlyReport generateMonthlyReport(LocalDate startDate, LocalDate endDate) {
        List<Loan> loansInPeriod = loanRepository.findByCheckoutDateBetween(startDate, endDate);
        long reportingPeriodDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        return MonthlyReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .reportingPeriodDays(reportingPeriodDays)
                .totalLoans(loansInPeriod.size())
                .activeLoans(countLoansByStatus(loansInPeriod, "ACTIVE"))
                .overdueLoans(countLoansByStatus(loansInPeriod, "OVERDUE"))
                .returnedLoans(countLoansByStatus(loansInPeriod, "RETURNED"))
                .avgLoansPerDay(calculateAverageLoansPerDay(loansInPeriod.size(), reportingPeriodDays))
                .overdueRate(calculateOverdueRate(loansInPeriod))
                .mostPopularBooks(findMostPopularBooks(loansInPeriod, 5))
                .memberActivitySummary(generateMemberActivitySummary(loansInPeriod))
                .build();
    }

    public OverdueReport generateOverdueReport() {
        List<Loan> overdueLoans = loanRepository.findOverdueLoans();

        return OverdueReport.builder()
                .reportDate(LocalDate.now())
                .totalOverdueLoans(overdueLoans.size())
                .overdueLoans(overdueLoans)
                .totalFinesOwed(calculateTotalFinesOwed(overdueLoans))
                .averageDaysOverdue(calculateAverageDaysOverdue(overdueLoans))
                .build();
    }

    public FineReport generateFineReport(LocalDate startDate, LocalDate endDate) {
        List<Fine> finesInPeriod = fineRepository.findByIssueDateBetween(startDate, endDate);

        return FineReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalFinesIssued(finesInPeriod.size())
                .totalFineAmount(calculateTotalFineAmount(finesInPeriod))
                .finesPaid(countFinesByStatus(finesInPeriod, "PAID"))
                .finesPending(countFinesByStatus(finesInPeriod, "PENDING"))
                .collectionRate(calculateFineCollectionRate(finesInPeriod))
                .build();
    }

    private int countLoansByStatus(List<Loan> loans, String status) {
        return (int) loans.stream()
                .filter(loan -> status.equals(loan.getStatus().name()))
                .count();
    }

    private int countFinesByStatus(List<Fine> fines, String status) {
        return (int) fines.stream()
                .filter(fine -> status.equals(fine.getStatus().name()))
                .count();
    }

    private double calculateAverageLoansPerDay(int totalLoans, long days) {
        return days > 0 ? (double) totalLoans / days : 0.0;
    }

    private double calculateOverdueRate(List<Loan> loans) {
        if (loans.isEmpty())
            return 0.0;
        long overdueCount = loans.stream()
                .filter(loan -> "OVERDUE".equals(loan.getStatus().name()))
                .count();
        return (double) overdueCount / loans.size() * 100;
    }

    private List<String> findMostPopularBooks(List<Loan> loans, int limit) {
        return loans.stream()
                .collect(Collectors.groupingBy(
                        loan -> loan.getBookCopy().getBook().getTitle(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> generateMemberActivitySummary(List<Loan> loans) {
        return loans.stream()
                .collect(Collectors.groupingBy(
                        loan -> loan.getMember().getUser().getFullName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private double calculateTotalFinesOwed(List<Loan> overdueLoans) {
        return overdueLoans.stream()
                .mapToDouble(loan -> {
                    Fine fine = fineService.createOverdueFine(loan);
                    return fine != null ? fine.getAmount().doubleValue() : 0.0;
                })
                .sum();
    }

    private double calculateAverageDaysOverdue(List<Loan> overdueLoans) {
        if (overdueLoans.isEmpty())
            return 0.0;

        return overdueLoans.stream()
                .mapToInt(loan -> (int) ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now()))
                .average()
                .orElse(0.0);
    }

    private double calculateTotalFineAmount(List<Fine> fines) {
        return fines.stream()
                .mapToDouble(fine -> fine.getAmount().doubleValue())
                .sum();
    }

    private double calculateFineCollectionRate(List<Fine> fines) {
        if (fines.isEmpty())
            return 0.0;

        long paidFines = fines.stream()
                .filter(fine -> "PAID".equals(fine.getStatus().name()))
                .count();

        return (double) paidFines / fines.size() * 100;
    }

    @Data
    @Builder
    public static class MonthlyReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private long reportingPeriodDays;
        private int totalLoans;
        private int activeLoans;
        private int overdueLoans;
        private int returnedLoans;
        private double avgLoansPerDay;
        private double overdueRate;
        private List<String> mostPopularBooks;
        private Map<String, Integer> memberActivitySummary;
    }

    @Data
    @Builder
    public static class OverdueReport {
        private LocalDate reportDate;
        private int totalOverdueLoans;
        private List<Loan> overdueLoans;
        private double totalFinesOwed;
        private double averageDaysOverdue;
    }

    @Data
    @Builder
    public static class FineReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalFinesIssued;
        private double totalFineAmount;
        private int finesPaid;
        private int finesPending;
        private double collectionRate;
    }
}