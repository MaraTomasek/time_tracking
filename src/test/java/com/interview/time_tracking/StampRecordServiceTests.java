package com.interview.time_tracking;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

import com.interview.time_tracking.dao.StampRecordRepository;
import com.interview.time_tracking.model.StampRecord;
import com.interview.time_tracking.service.StampRecordService;

@ExtendWith(MockitoExtension.class)
public class StampRecordServiceTests {

    @Mock
    StampRecordRepository stampRecordRepository;

    @InjectMocks
    StampRecordService stampRecordService;

    private final Long checkIn1 = 1736060400000L;
    private final Long checkOut1 = 1736074800000L;
    private final PageRequest maximumPageable = PageRequest.of(0, 1,
            Sort.by(Sort.Direction.DESC, "checkInInMilliseconds"));
    private final StampRecord filter1 = new StampRecord(3001L, 0L, checkIn1, checkOut1); // 2025-01-05 08:00-12:00
    private final StampRecord filter2 = new StampRecord(3002L, 0L, 1736492400000L, 1736517600000L); // 2025-01-10
                                                                                                    // 08:00-15:00
    private final StampRecord filter3 = new StampRecord(3003L, 0L, 1737356400000L, 1737399600000L); // 2025-01-20
                                                                                                    // 08:00-20:00
    private final StampRecord filter4 = new StampRecord(3004L, 1L, 1737356400000L, 1737385200000L); // 2025-01-20
                                                                                                    // 08:00-16:00
    private final StampRecord filter5 = new StampRecord(3005L, 1L, 1737356400000L, null); // 2025-01-20 08:00-16:00

    private final StampRecord[] filteredStampRecordsArr = new StampRecord[] { filter1, filter2 };

    private List<StampRecord> filteredStampRecords;

    @BeforeEach
    public void setUp() {
        this.filteredStampRecords = new ArrayList<>();
        this.filteredStampRecords.addAll(List.of(filteredStampRecordsArr));
    }

    @Test
    void shouldFilterStampRecordsCorrectly() {
        long stampUserId = 0L;
        long rangeStartDate = 1735704000000L; // 2025-01-01 05:00
        long rangeEndDate = 1736974800000L; // 2025-01-15 20:00

        given(stampRecordRepository.findByUserIdAndCheckInInMillisecondsBetween(
                stampUserId,
                rangeStartDate,
                rangeEndDate))
                .willReturn(filteredStampRecords);

        List<StampRecord> filteredStampRecords = stampRecordService.getStampRecordsWithCheckinDateBetween(
                stampUserId,
                rangeStartDate,
                rangeEndDate);
        assertEquals(this.filteredStampRecords, filteredStampRecords);
    }

    @Test
    void shouldCalculateCheckedInTimeCorrectly() {
        long stampRecordId = 3001L;
        given(stampRecordRepository.findById(stampRecordId)).willReturn(Optional.of(this.filter1));

        Duration calculatedDuration = stampRecordService.calculateCheckedInTime(stampRecordId);
        Duration expectedDuration = Duration.between(
                Instant.ofEpochMilli(checkIn1),
                Instant.ofEpochMilli(checkOut1));

        assertEquals(expectedDuration, calculatedDuration);
    }

    @Test
    void shouldThrowWhenNoEntryToCalculateCheckedInTime() {
        long unknownStampRecordId = 999L;
        given(stampRecordRepository.findById(unknownStampRecordId)).willReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            stampRecordService.calculateCheckedInTime(unknownStampRecordId);
        });
    }

    @Test
    void shouldCalculateHoursWorkedCorrectlyForBigBreak() {
        long longBreakId = 3003L;
        given(stampRecordRepository.findById(longBreakId)).willReturn(Optional.of(filter3));

        Duration actualDuration = stampRecordService.calculateHoursWorked(longBreakId);
        Duration expectedDuration = Duration.ofHours(11).plusMinutes(15);

        assertEquals(expectedDuration, actualDuration);
    }

    @Test
    void shouldCalculateHoursWorkedCorrectlyForSmallBreak() {
        long smallBreakId = 3002L;
        given(stampRecordRepository.findById(smallBreakId)).willReturn(Optional.of(filter2));

        Duration actualDuration = stampRecordService.calculateHoursWorked(smallBreakId);
        Duration expectedDuration = Duration.ofHours(6).plusMinutes(30);

        assertEquals(expectedDuration, actualDuration);
    }

    @Test
    void shouldCalculateHoursWorkedCorrectlyForNoBreak() {
        long noBreakId = 3001L;
        given(stampRecordRepository.findById(noBreakId)).willReturn(Optional.of(filter1));

        Duration actualDuration = stampRecordService.calculateHoursWorked(noBreakId);
        Duration expectedDuration = Duration.ofHours(4);

        assertEquals(expectedDuration, actualDuration);
    }

    @Test
    void shouldCalculateHoursWorkedCorrectlyOnEdgeCase() {
        long noBreakId = 3004L;
        given(stampRecordRepository.findById(noBreakId)).willReturn(Optional.of(filter4));

        Duration actualDuration = stampRecordService.calculateHoursWorked(noBreakId);
        Duration expectedDuration = Duration.ofHours(7).plusMinutes(30);

        assertEquals(expectedDuration, actualDuration);
    }

    @Test
    void shouldValidateCheckedInStatus() {
        long checkedOutId = 3004L;
        long checkedInId = 3005L;
        Page<StampRecord> pageOfCheckedOutId = new PageImpl<StampRecord>(List.of(filter4));
        Page<StampRecord> pageOfCheckedInId = new PageImpl<StampRecord>(List.of(filter5));
        given(stampRecordRepository.findByUserId(checkedOutId, maximumPageable)).willReturn(pageOfCheckedOutId);
        given(stampRecordRepository.findByUserId(checkedInId, maximumPageable)).willReturn(pageOfCheckedInId);

        boolean isCheckedInActual = stampRecordService.isStampedInAlready(checkedInId);
        boolean isCheckedOutActual = stampRecordService.isStampedInAlready(checkedOutId);

        assertEquals(true, isCheckedInActual);
        assertEquals(false, isCheckedOutActual);
    }

}
