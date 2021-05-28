package tech.itpark.dto;

import lombok.Value;

import java.sql.Timestamp;
import java.util.Set;

@Value
public class AppointmentFindRequestDto {
    Timestamp startTimestamp;
    Timestamp endTimestamp;
    Set<Long> doctorIds;
    Set<Long> patientIds;
    Set<Integer> statuses;
    boolean own;
    boolean available;
}
