package com.interview.time_tracking.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.interview.time_tracking.dao.StampRecordRepository;
import com.interview.time_tracking.model.BreaktimeRules;
import com.interview.time_tracking.model.StampRecord;

@Service
public class StampRecordService {

    private final StampRecordRepository stampRecordRepository;

    private StampRecordService(StampRecordRepository stampRecordRepository) {
        this.stampRecordRepository = stampRecordRepository;
    }

    public boolean isStampedInAlready(Long stampUserId) {
        List<StampRecord> latestStampRecords = stampRecordRepository.findByUserId(
                stampUserId,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "checkInInMilliseconds")))
                .getContent();

        if (latestStampRecords.isEmpty()) {
            return false;
        }

        StampRecord latestStampRecord = latestStampRecords.get(0);
        return latestStampRecord.getCheckOutInMilliseconds() == null;
    }

    public List<StampRecord> getStampRecordsWithCheckinDateBetween(Long stampUserId, Long startDateinMilliseconds,
            Long endDateInMilliseconds) {
        List<StampRecord> stampRecordsInDateRange = stampRecordRepository
                .findByUserIdAndCheckInInMillisecondsBetween(stampUserId, startDateinMilliseconds,
                        endDateInMilliseconds);

        return stampRecordsInDateRange;
    }

    public Duration calculateCheckedInTime(Long stampRecordId) throws NoSuchElementException {
        Optional<StampRecord> optionalStampRecord = this.stampRecordRepository.findById(stampRecordId);

        if (optionalStampRecord.isEmpty()) {
            throw new NoSuchElementException(String.format("StampRecord with Id %d not found", stampRecordId));
        }

        StampRecord stampRecord = optionalStampRecord.get();
        Instant startTime = Instant.ofEpochMilli(stampRecord.getCheckInInMilliseconds());
        Instant endTime = Instant.ofEpochMilli(stampRecord.getCheckOutInMilliseconds());

        return Duration.between(startTime, endTime);
    }

    public Duration calculateHoursWorked(Long stampRecordId) {
        final Duration checkedInTime = calculateCheckedInTime(stampRecordId);

        if (doesNeedBigBreak(checkedInTime)) {
            return checkedInTime.minus(BreaktimeRules.BIG_BREAK_DURATION);
        } else if (doesNeedSmallBreak(checkedInTime)) {
            return checkedInTime.minus(BreaktimeRules.SMALL_BREAK_DURATION);
        }

        return checkedInTime;
    }

    private boolean doesNeedBigBreak(Duration duration) {
        // We use '>=' to be consistent on edge cases, like working exactly 8h
        return duration.compareTo(BreaktimeRules.BIG_BREAK_LIMIT) >= 0;
    }

    private boolean doesNeedSmallBreak(Duration duration) {
        return duration.compareTo(BreaktimeRules.SMALL_BREAK_LIMIT) >= 0;
    }

}
