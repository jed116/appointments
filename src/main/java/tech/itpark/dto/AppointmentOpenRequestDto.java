package tech.itpark.dto;

import lombok.Value;

import java.sql.Timestamp;

@Value
public class AppointmentOpenRequestDto {
    Timestamp date_time;
}
