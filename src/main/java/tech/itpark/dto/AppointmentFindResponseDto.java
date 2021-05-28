package tech.itpark.dto;

import lombok.Value;
import tech.itpark.model.Appointment;

import java.util.List;

@Value public class AppointmentFindResponseDto {
    List<Appointment> appointments;
}
