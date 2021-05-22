package tech.itpark.dto;

import lombok.Value;

import java.sql.Timestamp;

@Value
public class AppointCreateRequestDto {
    Timestamp date_time;
    long doctor_id;
}
