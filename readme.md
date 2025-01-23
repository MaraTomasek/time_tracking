# Exercise - Time Tracking
Finding good Names was very difficult. I found no catchy translation of "Stempeluhr" or related Terms, `Record` is a reserved keyword in Java and `Timestamp` a reserved keyword in many databases.

I chose following Substitutes for these Keywords:
- Stempeluhr - Time Tracking
- Zeiterfassungseintrag - StampRecord
- Benutzer - StampUser

## 1. Create a Data Model
`./src/main/java/com/interview/time_tracking/model`
- I chose to create a simple data model. Easily extendable though.

## 2. Repository Request
`./src/main/java/com/interview/time_tracking/dao/StampRecordRepository.java`
- I chose to use the automatic JPA Query.

## 3. Calculate Hours Worked
```./src/main/java/com/interview/time_tracking/dao/StampRecordService.java/getStampRecordsWithCheckinDateBetween```
- All records of a certain User within a given Date Range:

`./src/main/java/com/interview/time_tracking/dao/StampRecordService.java/calculateHoursWorked`
- Calculate the worked hours and subtract break times given by law:

`./src/test/java/com/interview/time_tracking/StampRecordServiceTests.java`
- Location of the Tests

## 4. REST-API
`./src/main/java/com/interview/time_tracking/controller/StampRecordController.java`
- All REST-API functionalities can be found here 
- I also added an extra GET-Endpoint at `"/stamp-records/{userId}"` with Pagination Support. Here one can request all Records of a User.
- The API Documentation can be found at ```"/swagger-ui/index.html"```

`./src/test/java/com/interview/time_tracking/TimeTrackingApplicationTests.java`
- Location of the Tests

## 5. Exception Handling
Where necessary I implemented Exception Handling, though this is mostly handled through Http Status-Codes.

## 6. Bonus Exercise
`./src/main/java/com/interview/time_tracking/dao/StampRecordService.java/isStampedInAlready`
- The method to check if a User is checked-in already
- It is called in the Post Endpoint at ```"/stamp-records"```.
