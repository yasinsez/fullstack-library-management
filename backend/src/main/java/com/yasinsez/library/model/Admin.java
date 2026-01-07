package com.yasinsez.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "admins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(name = "admin_level")
    private Integer adminLevel;

    @Column(length = 100)
    private String department;

    @Column(columnDefinition = "TEXT")
    private String permissions;

    @Column(name = "last_activity")
    private LocalDate lastActivity;
}
