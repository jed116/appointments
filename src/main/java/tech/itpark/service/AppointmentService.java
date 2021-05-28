package tech.itpark.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import tech.itpark.configuration.AppParams;
import tech.itpark.dto.*;
import tech.itpark.model.Appointment;
import tech.itpark.model.User;
import tech.itpark.repository.AppointmentRepository;
import tech.itpark.security.Auth;

import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Timestamp;


@Service
@RequiredArgsConstructor
public class AppointmentService{
    private final AppointmentRepository repository;

    private long minimalAppointmentTime;
    private long startAppointmentPeriod;
    private long endAppointmentPeriod;
    private long appointmentDayLimit;

    public void initParams(long minimalAppointmentTime, long startAppointmentPeriod, long endAppointmentPeriod, long appointmentDayLimit) {
        this.minimalAppointmentTime = minimalAppointmentTime;
        this.startAppointmentPeriod = startAppointmentPeriod;
        this.endAppointmentPeriod = endAppointmentPeriod;
        this.appointmentDayLimit = appointmentDayLimit;
    }


    public AppointmentOpenResponseDto open(AppointmentOpenRequestDto requestDto, Auth auth) {
        long doctor_id = auth.getId();
        if (doctor_id <= 0){
            throw new RuntimeException("USER NOT AUTHORIZED !!!");
        }

        User user = (User) auth;
        String doctorFirstName = user.getFirstName();
        String doctorSecondName = user.getSecondName();
        String doctorDescription = user.getDescription();
        if (doctorFirstName.trim().isEmpty()  || doctorSecondName.trim().isEmpty() || doctorDescription.trim().isEmpty()){
            throw new RuntimeException("NO DOCTOR INFORMATION !!!");
        }

        Timestamp appointmentTimestamp = requestDto.getDate_time();
        java.util.Date date = new java.util.Date();
        Timestamp startTimestamp    = new Timestamp(atStartOfDay(date).getTime() + (startAppointmentPeriod * (86400 * 1000)));
        Timestamp endTimestamp      = new Timestamp(atEndOfDay(date).getTime() + (endAppointmentPeriod * (86400 * 1000)));

        if (appointmentTimestamp.before(startTimestamp) || appointmentTimestamp.after(endTimestamp)){
            final var startDate = new Date(startTimestamp.getTime());
            final var endDate = new Date(endTimestamp.getTime());
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

            throw new RuntimeException("WRONG PERIOD !!! Valid days from " +
                    formatter.format(startDate)  + " to " + formatter.format(endDate));
        }

        Timestamp beforeTimestamp   = new Timestamp(appointmentTimestamp.getTime() - (minimalAppointmentTime * 1000));
        Timestamp afterTimestamp    = new Timestamp(appointmentTimestamp.getTime() + (minimalAppointmentTime * 1000));
        List<Appointment> appointments = repository.find(beforeTimestamp, afterTimestamp, Set.of(doctor_id) , Set.of(), Set.of(0),
                true, true, false);
        if (appointments.size() > 0){
            final var conflictAppointmentId = appointments.get(0).getId();
            final var conflictAppointmentDateTime = new Date(appointments.get(0).getDateTime().getTime());
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

            throw new RuntimeException("WRONG TIME !!! Conflict with other appointment No. " +
                    conflictAppointmentId.toString() + " of " + formatter.format(conflictAppointmentDateTime));
        }

        Appointment appointment = repository.open( new Appointment(0L,appointmentTimestamp, 0,
                doctor_id, doctorFirstName, doctorSecondName, doctorDescription,
                0L, "", "", "", "", "")
        );

        final var id = appointment.getId();
        if ( id == 0){
            throw new RuntimeException("ERROR CREATING APPOINTMENT !!!");
        }

        return new AppointmentOpenResponseDto(id);
    }

    public AppointmentBookResponseDto book(AppointmentBookRequestDto requestDto, Auth auth) {
        long patient_id = auth.getId();
        if (patient_id <= 0){
            throw new RuntimeException("USER NOT AUTHORIZED !!!");
        }

        User user = (User) auth;
        String patientFirstName = user.getFirstName();
        String patientSecondName = user.getSecondName();
        String patientDescription = user.getDescription();
        if (patientFirstName.trim().isEmpty()  || patientSecondName.trim().isEmpty() || patientDescription.trim().isEmpty()){
            throw new RuntimeException("NO PATIENT INFORMATION !!!");
        }

        Optional<Appointment> appointmentOptional = repository.getById(requestDto.getId());
        if (appointmentOptional.isEmpty()){
            throw new RuntimeException("WRONG APPOINTMENT ID !!!");
        }
        final var appointment = appointmentOptional.get();

        if (appointment.getStatus() != 0){
            throw new RuntimeException("WRONG APPOINTMENT STATUS !!!");
        }

        if (appointment.getPatient_id() != null && appointment.getPatient_id() > 0 ){
            throw new RuntimeException("APPOINTMENT ALREADY BOOKED!!!");
        }

        final var appointmentDate = new Date(appointment.getDateTime().getTime());
        final var appointmentsForDay = repository.find( new Timestamp(atStartOfDay(appointmentDate).getTime()),
                                                                        new Timestamp(atEndOfDay(appointmentDate).getTime()),
                                                                            Set.of(), Set.of(patient_id), Set.of(0, 1),
                                                            false, false, false);
        if ( appointmentsForDay.size() >= appointmentDayLimit){
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            throw new RuntimeException("As of "+ formatter.format(appointmentDate) +" appointments day limit exceeded !!!");
        }

        appointment.setAccessCode(RandomStringUtils.random(8, "0123456789"));
        repository.book(appointment, user);

        return new AppointmentBookResponseDto(appointment.getId(), appointment.getAccessCode());
    }

    public AppointmentUnBookResponseDto unBook(AppointmentUnBookRequestDto requestDto, Auth auth) {
        long patient_id = auth.getId();
        if (patient_id <= 0){
            throw new RuntimeException("USER NOT AUTHORIZED !!!");
        }

        User user = (User) auth;
        final var userRoles = user.getRoles();

        boolean isPatient = AppParams.isPatient(userRoles);
        boolean isChief = AppParams.isChief(userRoles);

        if (!(isPatient || isChief)){
            throw new RuntimeException("PERMISSION ERROR!!! Only patients and chiefs can un-book an appointment.");
        }

        Optional<Appointment> appointmentOptional = repository.getById(requestDto.getId());
        if (appointmentOptional.isEmpty()){
            throw new RuntimeException("WRONG APPOINTMENT ID !!!");
        }
        final var appointment = appointmentOptional.get();

        final var status = appointment.getStatus();
        if (status != 0){
            throw new RuntimeException("WRONG APPOINTMENT STATUS !!! Appointment " + (status > 0 ? "is closed." : "is canceled"));
        }

        if (appointment.getPatient_id() == null || appointment.getPatient_id() == 0 ){
            throw new RuntimeException("NOT BOOKED APPOINTMENT CAN'T BE UN-BOOKED!!!");
        }

        if (isChief){
            repository.unBook(appointment);
            return new AppointmentUnBookResponseDto(appointment.getId());
        }

        if (appointment.getPatient_id() != user.getId()){
            throw new RuntimeException("PATIENT CAN'T UN-BOOK NOT OWN APPOINTMENT !!!");
        }
        repository.unBook(appointment);
        return new AppointmentUnBookResponseDto(appointment.getId());
    }

    public AppointmentCloseResponseDto close(AppointmentCloseRequestDto requestDto, Auth auth) {
        long user_id = auth.getId();
        if (user_id <= 0){
            throw new RuntimeException("USER NOT AUTHORIZED !!!");
        }

        User user = (User) auth;
        final var userRoles = user.getRoles();

        boolean isDoctor = AppParams.isDoctor(userRoles);
        boolean isChief = AppParams.isChief(userRoles);

        if (!(isDoctor || isChief)){
            throw new RuntimeException("PERMISSION ERROR!!! Only doctors or chiefs can close an appointment.");
        }

        Optional<Appointment> appointmentOptional = repository.getById(requestDto.getId());
        if (appointmentOptional.isEmpty()){
            throw new RuntimeException("WRONG APPOINTMENT ID !!!");
        }
        final var appointment = appointmentOptional.get();

        final var status = appointment.getStatus();
        if (status != 0){
            throw new RuntimeException("WRONG APPOINTMENT STATUS !!! Appointment " + (status > 0 ? "already closed." : "is canceled"));
        }

        if (appointment.getPatient_id() == null || appointment.getPatient_id() == 0 ){
            throw new RuntimeException("CAN'T CLOSE UN-BOOKED APPOINTMENT!!!");
        }

        if (isDoctor){
            if (appointment.getDoctor_id() != user.getId()){
                throw new RuntimeException("DOCTOR CAN'T CLOSE NOT OWN APPOINTMENT !!!");
            }
            if (!requestDto.getAccessCode().equals(appointment.getAccessCode())){
                throw new RuntimeException("WRONG ACCESS CODE !!!");
            }
        }else {
            appointment.setAccessCode("--------");
        }

        appointment.setStatus(1);
        appointment.setResult(requestDto.getResult());
        repository.close(appointment);

        return new AppointmentCloseResponseDto(appointment.getId());
    }

    public AppointmentCancelResponseDto cancel(AppointmentCancelRequestDto requestDto, Auth auth) {
        long user_id = auth.getId();
        if (user_id <= 0){
            throw new RuntimeException("USER NOT AUTHORIZED !!!");
        }

        User user = (User) auth;
        final var userRoles = user.getRoles();

        boolean isDoctor = AppParams.isDoctor(userRoles);
        boolean isChief = AppParams.isChief(userRoles);

        if (!(isDoctor || isChief)){
            throw new RuntimeException("PERMISSION ERROR!!! Only patients, doctors and chiefs can cancel an appointment.");
        }

        Optional<Appointment> appointmentOptional = repository.getById(requestDto.getId());
        if (appointmentOptional.isEmpty()){
            throw new RuntimeException("WRONG APPOINTMENT ID !!!");
        }
        final var appointment = appointmentOptional.get();

        final var status = appointment.getStatus();
        if (status != 0){
            throw new RuntimeException("WRONG APPOINTMENT STATUS !!! Appointment " + (status > 0 ? "is closed." : "already canceled"));
        }

        if (isChief){
            if (appointment.getPatient_id() != null && appointment.getPatient_id() > 0 ){
                throw new RuntimeException("CAN'T CANCEL ALREADY BOOKED APPOINTMENT!!!");
            }
            appointment.setStatus(-1);
            repository.cancel(appointment);
            return new AppointmentCancelResponseDto(appointment.getId());
        }

        if (isDoctor){
            if (appointment.getPatient_id() != null && appointment.getPatient_id() > 0 ){
                throw new RuntimeException("CAN'T CANCEL ALREADY BOOKED APPOINTMENT!!!");
            }
            if (appointment.getDoctor_id() != user.getId()){
                throw new RuntimeException("DOCTOR CAN'T CANCEL NOT OWN APPOINTMENT !!!");
            }
            appointment.setStatus(-1);
            repository.cancel(appointment);
            return new AppointmentCancelResponseDto(appointment.getId());
        }

        throw new RuntimeException("UNKNOWN PERMISSION ERROR DURING CANCEL APPOINTMENT OPERATION!!!");
    }

    public AppointmentFindResponseDto find(AppointmentFindRequestDto requestDto, Auth auth) {
        long user_id = auth.getId();
        if (user_id <= 0){
            throw new RuntimeException("USER NOT AUTHORIZED !!!");
        }

        User user = (User) auth;
        final var userRoles = user.getRoles();

        boolean isAdmin = AppParams.isAdmin(userRoles);
        boolean isChief = AppParams.isChief(userRoles);
        boolean isDoctor = AppParams.isDoctor(userRoles);
        boolean isPatient = AppParams.isPatient(userRoles);

        final var startTimestamp = requestDto.getStartTimestamp();
        final var endTimestamp = requestDto.getEndTimestamp();
        final var doctorIds = requestDto.getDoctorIds();
        final var patientIds = requestDto.getPatientIds();
        final var statuses = requestDto.getStatuses();
        final var ownOnly = requestDto.isOwn();
        final var availableOnly = requestDto.isAvailable();
        boolean maskedAccessCode = false;
        boolean maskedPatientInfo = false;

        if (startTimestamp.compareTo(endTimestamp) >= 0){
            throw new RuntimeException("WRONG FIND PERIOD !!!");
        }

        if(isPatient && !(isAdmin || isChief || isDoctor)){
            maskedAccessCode = true;
            maskedPatientInfo = true;
            patientIds.clear();
            if (ownOnly){
                patientIds.add(user_id);
                maskedAccessCode = false;
                maskedPatientInfo = false;
            }
            statuses.clear();
            statuses.add(0);
        }

        if((isDoctor) && !(isAdmin || isChief)){
            maskedAccessCode = true;
            maskedPatientInfo = true;
            if (ownOnly){
                doctorIds.clear();
                doctorIds.add(user_id);
                maskedPatientInfo = false;
            }else{
                patientIds.clear();
            }
        }

        final var appointments = repository.find(startTimestamp, endTimestamp, doctorIds, patientIds, statuses,
                maskedAccessCode, maskedPatientInfo, availableOnly);

        return new AppointmentFindResponseDto(appointments);
    }



    private Date atStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date atEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }


}
