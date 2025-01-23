package com.interview.time_tracking.model;

import java.time.Duration;

public class BreaktimeRules {
    public final static Duration BIG_BREAK_LIMIT = Duration.ofHours(9);
    public final static Duration SMALL_BREAK_LIMIT = Duration.ofHours(6);

    public final static Duration BIG_BREAK_DURATION = Duration.ofMinutes(45);
    public final static Duration SMALL_BREAK_DURATION = Duration.ofMinutes(30);

}
