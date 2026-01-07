package com.yasinsez.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(name = "membership_start_date")
    private LocalDate membershipStartDate;

    @Column(name = "membership_end_date")
    private LocalDate membershipEndDate;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "fine_balance", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fineBalance = BigDecimal.ZERO;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Loan> loans = new HashSet<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Reservation> reservations = new HashSet<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Fine> fines = new HashSet<>();

    @PrePersist
    @PreUpdate
    protected void ensureDefaults() {
        if (this.fineBalance == null) {
            this.fineBalance = BigDecimal.ZERO;
        }
    }

    @PostLoad
    protected void applyDefaultsOnLoad() {
        ensureDefaults();
    }
}
