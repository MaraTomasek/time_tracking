package com.interview.time_tracking.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.UriComponentsBuilder;

import com.interview.time_tracking.dao.StampRecordRepository;
import com.interview.time_tracking.model.StampRecord;
import com.interview.time_tracking.service.StampRecordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RestControllerAdvice
@RequestMapping("/stamp-records")
class StampRecordController {

    StampRecordService stampRecordService;
    StampRecordRepository stampRecordRepository;

    private StampRecordController(StampRecordService stampRecordService, StampRecordRepository stampRecordRepository) {
        this.stampRecordService = stampRecordService;
        this.stampRecordRepository = stampRecordRepository;
    }

    @GetMapping("/all/{userId}")
    @Operation(summary = "Get all StampRecords of a User by their userId")
    @ApiResponse(
        responseCode = "200", 
        description = "Found List of StampRecords",
        content = { @Content(
            mediaType = "application/json", 
            array = @ArraySchema(
                schema = @Schema(
                    implementation = StampRecord.class)))})
    public ResponseEntity<List<StampRecord>> getAllByUserId(@PathVariable Long userId, Pageable pageable) {
        Page<StampRecord> pageOfStampRecords = stampRecordRepository.findByUserId(
                userId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "checkInInMilliseconds"))));

        return ResponseEntity.ok(pageOfStampRecords.getContent());
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "Get a StampRecord by its id")
    @ApiResponse(
        responseCode = "200", 
        description = "Found the StampRecord",
        content = { @Content(
            mediaType = "application/json", 
            schema = @Schema(
                implementation = StampRecord.class))})
    @ApiResponse(
        responseCode = "404", 
        description = "Did not find the StampRecord",
        content = @Content(schema=@Schema(implementation = Void.class)))
    public ResponseEntity<StampRecord> getById(@PathVariable Long recordId) {
        Optional<StampRecord> optionalStampRecord = stampRecordRepository.findById(recordId);

        if (optionalStampRecord.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(optionalStampRecord.get());
    }

    @PostMapping
    @Operation(
        summary = "Create a new StampRecord", 
        description = "- Supplied StampRecord Must be valid\n- User can only check-in if he's not already checked in")
    @ApiResponse(
        responseCode = "201", 
        description = "Created the StampRecord",
        content = { @Content(
            mediaType = "application/json", 
            schema = @Schema(
                implementation = URI.class))})
    @ApiResponse(
        responseCode = "400", 
        description = "The supplied StampRecord is not valid or User is already checked-in",
        content = @Content(schema=@Schema(implementation = Void.class)))
    public ResponseEntity<Void> create(@RequestBody StampRecord newStampRecord, UriComponentsBuilder ucb) {
        boolean isRecordStampInvalid = !newStampRecord.isValidStampRecord();
        boolean isUserAlreadyCheckedIn = stampRecordService.isStampedInAlready(newStampRecord.getUserId());

        if (isRecordStampInvalid || isUserAlreadyCheckedIn) {
            return ResponseEntity.badRequest().build();
        }

        StampRecord savedStampRecord = stampRecordRepository.save(newStampRecord);
        URI locationOfNewStampRecord = ucb
                .path("stamp-records/{id}")
                .buildAndExpand(savedStampRecord.getId())
                .toUri();
        return ResponseEntity.created(locationOfNewStampRecord).build();
    }

    @PutMapping("/{recordId}")
    @Operation(
        summary = "Update a StampRecord", 
        description = "- Supplied StampRecord Must be valid")
    @ApiResponse(
        responseCode = "204", 
        description = "Updated the StampRecord",
        content = @Content(schema=@Schema(implementation = Void.class)))
    @ApiResponse(
        responseCode = "400", 
        description = "The supplied StampRecord is not valid",
        content = @Content(schema=@Schema(implementation = Void.class)))
    @ApiResponse(
        responseCode = "404", 
        description = "The StampRecord does not exist",
        content = @Content(schema=@Schema(implementation = Void.class)))
    public ResponseEntity<StampRecord> update(@PathVariable Long recordId,
            @RequestBody StampRecord changedStampRecord) {
        Optional<StampRecord> optionalStampRecord = stampRecordRepository.findById(recordId);

        if (optionalStampRecord.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!changedStampRecord.isValidStampRecord()) {
            return ResponseEntity.badRequest().build();
        }

        StampRecord newStampRecord = new StampRecord(
                recordId,
                changedStampRecord.getUserId(),
                changedStampRecord.getCheckInInMilliseconds(),
                changedStampRecord.getCheckOutInMilliseconds());

        stampRecordRepository.save(newStampRecord);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{recordId}")
    @Operation(summary = "Delete a StampRecord")
    @ApiResponse(
        responseCode = "204", 
        description = "Deleted the StampRecord",
        content = @Content(schema=@Schema(implementation = Void.class)))
    @ApiResponse(
        responseCode = "404", 
        description = "The StampRecord does not exist",
        content = @Content(schema=@Schema(implementation = Void.class)))
    public ResponseEntity<Void> delete(@PathVariable Long recordId) {
        if (!stampRecordRepository.existsById(recordId)) {
            return ResponseEntity.notFound().build();
        }

        stampRecordRepository.deleteById(recordId);
        return ResponseEntity.noContent().build();
    }
}