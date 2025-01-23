package com.interview.time_tracking.model;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StampRecord {

    @Id
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private Long checkInInMilliseconds;

    private Long checkOutInMilliseconds;

    @JsonIgnore
    public boolean isValidStampRecord() {

        // We do this because it doesn't make sense to have a record that only has a
        // check-out time, or to have a record which has no assigned user
        if (userId == null) {
            return false;
        }

        if (checkInInMilliseconds != null) {
            if (checkOutInMilliseconds != null) {
                return checkInInMilliseconds < checkOutInMilliseconds;
            }
            return true;
        }
        return false;
    }

}
