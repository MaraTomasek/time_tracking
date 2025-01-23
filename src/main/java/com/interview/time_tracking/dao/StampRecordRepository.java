package com.interview.time_tracking.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.interview.time_tracking.model.StampRecord;

public interface StampRecordRepository extends
                CrudRepository<StampRecord, Long>,
                PagingAndSortingRepository<StampRecord, Long> {

        Page<StampRecord> findByUserId(Long stampUserId, PageRequest PageRequest);

        // We consider records to be within a date range if the check-in time is within the range
        List<StampRecord> findByUserIdAndCheckInInMillisecondsBetween(Long stampUserId, Long startTimestamp,
                        Long endTimestamp);

}
