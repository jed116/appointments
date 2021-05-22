package tech.itpark.model;

import lombok.Value;

import java.sql.Timestamp;

@Value
public class Appointment {
    long id;
    Timestamp date_time;
    User doctor;
    User patient;
}
