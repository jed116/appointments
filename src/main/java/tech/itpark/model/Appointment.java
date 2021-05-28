package tech.itpark.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@AllArgsConstructor
@Data
public class Appointment {
    Long id;
    Timestamp dateTime;
    Integer status; // -1=canceled, 0=opened, 1=closed

    Long doctor_id;
    String doctorFirstName;
    String doctorSecondName;
    String doctorDescription;

    Long patient_id;
    String patientFirstName;
    String patientSecondName;
    String patientDescription;

    String accessCode;
    String result;
}
