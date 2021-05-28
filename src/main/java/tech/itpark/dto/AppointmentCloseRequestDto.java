package tech.itpark.dto;

import lombok.Value;

@Value
public class AppointmentCloseRequestDto {
    Long id;
    String accessCode;
    String result;
}
