package tech.itpark.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tech.itpark.exception.DataAccessException;
import tech.itpark.model.Appointment;
import tech.itpark.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class AppointmentRepository {
    private final DataSource dataSources;

    public Optional<Appointment> getById(long id) {
        if (id <=0){
            return Optional.empty();
        }
        try(
            final var connection = dataSources.getConnection();
            final var statement = connection.prepareStatement("""
          SELECT a.id AS id, a.dateTime AS dateTime, a.accesscode AS accessCode, a.status AS status, a.result AS result,
              a.doctor_id AS doctor_id, u_i_d.firstname AS doctorFirstName, u_i_d.secondname AS doctorSecondName, u_i_d.description AS doctorDescription, 
              a.patient_id AS patient_id, u_i_p.firstname AS patientFirstName, u_i_p.secondname AS patientSecondName, u_i_p.description AS patientDescription 
          FROM appointment a
          LEFT JOIN user_info u_i_d ON a.doctor_id = u_i_d.user_id
          LEFT JOIN user_info u_i_p ON a.patient_id = u_i_p.user_id
          WHERE a.id = ?
                        """, Statement.NO_GENERATED_KEYS);
           ){
            var index = 0;
            statement.setLong(++index, id);
            try (
                final var resultSet = statement.executeQuery();
            ) {
                return resultSet.next() ? Optional.of(appointmentFromResultSet(resultSet)) : Optional.empty();
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public List<Appointment> find(Timestamp startTimestamp, Timestamp endTimestamp,
                                  Set<Long> doctorIds, Set<Long> patientIds, Set<Integer> statuses,
                                  boolean maskedAccessCode, boolean maskedPatientInfo, boolean availableForBooking){
        List<Appointment> result = new ArrayList<>();

      try (
        final var connection = dataSources.getConnection();
        final var statement = connection.prepareStatement("""
          SELECT a.id AS id, a.dateTime AS dateTime, CASE WHEN ? THEN '*****' ELSE a.accesscode END AS accessCode, a.status AS status, a.result AS result,
              a.doctor_id AS doctor_id, u_i_d.firstname AS doctorFirstName, u_i_d.secondname AS doctorSecondName, u_i_d.description AS doctorDescription, 
              a.patient_id AS patient_id, CASE WHEN ? THEN 'N/А' ELSE u_i_p.firstname END AS patientFirstName,
                                          CASE WHEN ? THEN 'N/А' ELSE u_i_p.secondname END AS patientSecondName,
                                          CASE WHEN ? THEN 'N/А' ELSE u_i_p.description END  AS patientDescription 
          FROM appointment a
          LEFT JOIN user_info u_i_d ON a.doctor_id = u_i_d.user_id
          LEFT JOIN user_info u_i_p ON a.patient_id = u_i_p.user_id
          WHERE a.dateTime BETWEEN ? AND ? AND (doctor_id = ANY(?) OR ?) AND (patient_id = ANY(?) OR ?) AND (status = ANY(?) OR ?)
                  AND (? OR (a.patient_id = 0 OR a.patient_id IS NULL))      
                        """, Statement.NO_GENERATED_KEYS);
        ){
            var index = 0;
            statement.setBoolean(++index, maskedAccessCode);
            statement.setBoolean(++index, maskedPatientInfo);
            statement.setBoolean(++index, maskedPatientInfo);
            statement.setBoolean(++index, maskedPatientInfo);
            statement.setTimestamp(++index, startTimestamp);
            statement.setTimestamp(++index, endTimestamp);
            statement.setArray(++index,     connection.createArrayOf("BIGINT", doctorIds.toArray(Long[]::new)));
            statement.setBoolean(++index, doctorIds.size() == 0);
            statement.setArray(++index,     connection.createArrayOf("BIGINT", patientIds.toArray(Long[]::new)));
            statement.setBoolean(++index, patientIds.size() == 0);
            statement.setArray(++index,     connection.createArrayOf("INTEGER", statuses.toArray(Integer[]::new)));
            statement.setBoolean(++index, statuses.size() == 0);
            statement.setBoolean(++index, !availableForBooking);

            try (
                final var resultSet = statement.executeQuery();
            ) {
                while (resultSet.next()) {
                    result.add(new Appointment(
                            resultSet.getLong("id"),
                            resultSet.getTimestamp("dateTime"),
                            resultSet.getInt("status"),

                            resultSet.getLong("doctor_id"),
                            resultSet.getString("doctorFirstName"),
                            resultSet.getString("doctorSecondName"),
                            resultSet.getString("doctorDescription"),

                            resultSet.getLong("patient_id"),
                            resultSet.getString("patientFirstName"),
                            resultSet.getString("patientSecondName"),
                            resultSet.getString("patientDescription"),

                            resultSet.getString("accessCode"),
                            resultSet.getString("result")
                            )
                    );
                }
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return result;
    }

    public Appointment open(Appointment appointment) {
        try (
            final var connection = dataSources.getConnection();
            final var statement = connection.prepareStatement("""
                INSERT INTO appointment(dateTime, doctor_id, doctorFirstName, doctorSecondName, doctorDescription)
                VALUES (?, ?, ?, ?, ?);
            """, Statement.RETURN_GENERATED_KEYS); // альтернатива RETURNING
        ) {
            var index = 0;
            statement.setTimestamp(++index, appointment.getDateTime());
            statement.setLong(++index, appointment.getDoctor_id());
            statement.setString(++index, appointment.getDoctorFirstName());
            statement.setString(++index, appointment.getDoctorSecondName());
            statement.setString(++index, appointment.getDoctorDescription());
            statement.executeUpdate();

            final var keys = statement.getGeneratedKeys();
            if (!keys.next()) {
                throw new DataAccessException("no keys in result");
            }

            appointment.setId(keys.getLong(1));

            return appointment;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void book(Appointment appointment, User user) {
        try (
            final var statement = dataSources.getConnection().prepareStatement("""
               UPDATE appointment 
               SET patient_id = ?, patientFirstName = ?, patientSecondName = ?, patientDescription = ?, accessCode = ?
               WHERE id = ?
                       """)
        ) {
            var index = 0;
            statement.setLong(++index,   user.getId());
            statement.setString(++index, user.getFirstName());
            statement.setString(++index, user.getSecondName());
            statement.setString(++index, user.getDescription());
            statement.setString(++index, appointment.getAccessCode());
            statement.setLong(++index, appointment.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void unBook(Appointment appointment) {
        try (
                final var statement = dataSources.getConnection().prepareStatement("""
               UPDATE appointment 
               SET patient_id = NULL, patientFirstName = '', patientSecondName = '', patientDescription = '', accessCode  = '' 
               WHERE id = ?
                       """)
        ) {
            var index = 0;
            statement.setLong(++index, appointment.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void close(Appointment appointment) {
        try (
            final var statement = dataSources.getConnection().prepareStatement("""
               UPDATE appointment SET status = ?, accessCode = ?, result = ?  WHERE id = ?
                       """)
        ) {
            var index = 0;
            statement.setLong(++index,  appointment.getStatus());
            statement.setString(++index,appointment.getAccessCode());
            statement.setString(++index,appointment.getResult());
            statement.setLong(++index,  appointment.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void cancel(Appointment appointment) {
        try (
            final var statement = dataSources.getConnection().prepareStatement("""
                UPDATE appointment SET patient_id = ?, patientFirstName = ?, patientSecondName = ?, patientdescription = ?,
                accessCode = ?, status = ? 
                  WHERE id = ?
                       """)
        ) {
            var index = 0;
            final var patient_id = appointment.getPatient_id();
            final var patientFirstName = appointment.getPatientFirstName();
            final var patientSecondName = appointment.getPatientSecondName();
            final var patientDescription = appointment.getPatientDescription();
            final var accessCode = appointment.getAccessCode();

            if (patient_id != null && patient_id != 0){
                statement.setLong(++index, patient_id);
            }else {
                statement.setNull(++index, Types.BIGINT);
            }

            if (patientFirstName != null && patientFirstName != ""){
                statement.setString(++index, patientFirstName);
            }else {
                statement.setString(++index, "");
            }
            if (patientSecondName != null && patientSecondName != ""){
                statement.setString(++index, patientSecondName);
            }else {
                statement.setString(++index, "");
            }
            if (patientDescription != null && patientDescription != ""){
                statement.setString(++index, patientDescription);
            }else {
                statement.setString(++index, "");
            }
            if (accessCode != null && accessCode != ""){
                statement.setString(++index, accessCode);
            }else {
                statement.setString(++index, "");
            }

            statement.setInt(++index, appointment.getStatus());
            statement.setLong(++index, appointment.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Appointment appointmentFromResultSet(ResultSet resultSet) throws SQLException{
            return new Appointment(
                    resultSet.getLong("id"),
                    resultSet.getTimestamp("dateTime"),
                    resultSet.getInt("status"),

                    resultSet.getLong("doctor_id"),
                    resultSet.getString("doctorFirstName"),
                    resultSet.getString("doctorSecondName"),
                    resultSet.getString("doctorDescription"),

                    resultSet.getLong("patient_id"),
                    resultSet.getString("patientFirstName"),
                    resultSet.getString("patientSecondName"),
                    resultSet.getString("patientDescription"),

                    resultSet.getString("accessCode"),
                    resultSet.getString("result")

            );
    }

}
