package com.yasinsez.library.dto;

import com.yasinsez.library.model.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequestDTO(
                Long memberId,

                @NotNull(message = "Book ID is required") Long bookId,

                LocalDateTime reservationTime,

                ReservationStatus status,

                Integer priorityNumber) {
}
