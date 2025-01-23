package com.interview.time_tracking.model;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StampUser {

    @Id
    Long id;

    String userName;

}
