package com.interview.time_tracking;

import com.interview.time_tracking.model.StampRecord;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimeTrackingApplicationTests {

	private final static Long checkInTime = 1737356400000L;
	private final static Long largestTime = 1737702000000L;

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldReturnAnExistingStampRecord() {
		ResponseEntity<String> response = restTemplate.getForEntity("/stamp-records/1000", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		assertEquals(1000, id);

		Long checkInInMilliseconds = documentContext.read("$.checkInInMilliseconds");
		assertEquals(checkInTime, checkInInMilliseconds);
	}

	@Test
	void shouldNotReturnAStampRecordWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate.getForEntity("/stamp-records/9999", String.class);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
	}

	@Test
	void shouldReturnAPageOfStampRecords() {
		ResponseEntity<String> response = restTemplate.getForEntity("/stamp-records/all/0?page=0&size=1", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertEquals(1, read.size());
	}

	@Test
	void shouldReturnASortedPageOfStampRecords() {
		ResponseEntity<String> response = restTemplate
				.getForEntity("/stamp-records/all/0?page=0&size=1&sort=checkInInMilliseconds,desc", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertEquals(1, read.size());

		Number LargestCheckIn = documentContext.read("$[0].checkInInMilliseconds");
		assertEquals(largestTime, LargestCheckIn);
	}

	@Test
	void shouldReturnASortedPageOfStampRecordsWithNoParametersAndDefaultValues() {
		ResponseEntity<String> response = restTemplate.getForEntity("/stamp-records/all/0", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertEquals(5, read.size());

		Number LargestCheckIn = documentContext.read("$[0].checkInInMilliseconds");
		assertEquals(largestTime, LargestCheckIn);
	}

	@Test
	@DirtiesContext
	void shouldCreateANewStampRecord() {
		StampRecord newStampRecord = new StampRecord(null, 0L, 50L, 100L);
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/stamp-records", newStampRecord, Void.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		URI locationOfNewStampRecord = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewStampRecord, String.class);
		assertEquals(HttpStatus.OK, getResponse.getStatusCode());

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Number user_id = documentContext.read("$.userId");

		assertNotNull(id);
		assertEquals(0, user_id);
	}

	@Test
	void shouldNotCreateANewStampRecordWithInvalidData() {
		StampRecord newStampRecord = new StampRecord(null, null, 50L, 100L);
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/stamp-records", newStampRecord, Void.class);
		assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
	}

	@Test
	@DirtiesContext
	void shouldUpdateAnExistingStampRecord() {
		StampRecord stampRecordUpdate = new StampRecord(null, 1L, 50L, 100L);
		HttpEntity<StampRecord> request = new HttpEntity<>(stampRecordUpdate);
		ResponseEntity<Void> response = restTemplate.exchange("/stamp-records/1005", HttpMethod.PUT, request,
				Void.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

		ResponseEntity<String> getResponse = restTemplate.getForEntity("/stamp-records/1005", String.class);
		assertEquals(HttpStatus.OK, getResponse.getStatusCode());

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		assertEquals(1005, id);

		Number userId = documentContext.read("$.userId");
		assertEquals(1, userId);
	}

	@Test
	void shouldNotUpdateAStampRecordThatDoesNotExist() {
		StampRecord unknownStampRecord = new StampRecord(null, 2L, 50L, 100L);
		HttpEntity<StampRecord> request = new HttpEntity<>(unknownStampRecord);
		ResponseEntity<Void> response = restTemplate.exchange("/stamp-records/2000", HttpMethod.PUT, request,
				Void.class);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	void shouldNotUpdateAStampRecordWithBadRequest() {
		StampRecord unknownStampRecord = new StampRecord(null, 2L, null, 100L);
		HttpEntity<StampRecord> request = new HttpEntity<>(unknownStampRecord);
		ResponseEntity<Void> response = restTemplate.exchange("/stamp-records/1005", HttpMethod.PUT, request,
				Void.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	@DirtiesContext
	void sholdDeleteAnExistingStampRecord() {
		ResponseEntity<Void> response = restTemplate.exchange("/stamp-records/1004", HttpMethod.DELETE, null,
				Void.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

		ResponseEntity<String> getResponse = restTemplate.getForEntity("/stamp-records/1004", String.class);
		assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
	}

	@Test
	void sholdNotDeleteAStampRecordThatDoesNotExist() {
		ResponseEntity<Void> response = restTemplate.exchange("/stamp-records/2000", HttpMethod.DELETE, null,
				Void.class);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	@DirtiesContext
	void sholdNotLetAlreadyCheckedInUserCheckIn() {
		StampRecord checkIn = new StampRecord(null, 2L, 50L, null);
		StampRecord checkInAgain = new StampRecord(null, 2L, 60L, null);
		ResponseEntity<Void> response = restTemplate.postForEntity("/stamp-records", checkIn, Void.class);
		ResponseEntity<Void> responseAgain = restTemplate.postForEntity("/stamp-records", checkInAgain, Void.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(HttpStatus.BAD_REQUEST, responseAgain.getStatusCode());
	}

}
