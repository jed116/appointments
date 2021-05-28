package tech.itpark.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import tech.itpark.bodyconverter.BodyConverter;
import tech.itpark.dto.*;
import tech.itpark.http.ContentTypes;
import tech.itpark.security.HttpServletRequestAuthToken;
import tech.itpark.service.AppointmentService;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService service;
    private final List<BodyConverter> converters;


    public void initParams(long minimalAppointmentTime, long startAppointmentPeriod, long endAppointmentPeriod, long appointmentDayLimit){
        service.initParams(minimalAppointmentTime, startAppointmentPeriod, endAppointmentPeriod, appointmentDayLimit);
    }

    public void open(HttpServletRequest request, HttpServletResponse response){
        try {
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(AppointmentOpenRequestDto.class, request);
            final var responseDto = service.open(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

    public void book(HttpServletRequest request, HttpServletResponse response) {
        try{
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(AppointmentBookRequestDto.class, request);
            final var responseDto = service.book(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

    public void unbook(HttpServletRequest request, HttpServletResponse response) {
        try{
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(AppointmentUnBookRequestDto.class, request);
            final var responseDto = service.unBook(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }


    public void close(HttpServletRequest request, HttpServletResponse response) {
        try{
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(AppointmentCloseRequestDto.class, request);
            final var responseDto = service.close(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

    public void cancel(HttpServletRequest request, HttpServletResponse response) {
        try{
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(AppointmentCancelRequestDto.class, request);
            final var responseDto = service.cancel(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

    public void find(HttpServletRequest request, HttpServletResponse response) {
        try{
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(AppointmentFindRequestDto.class, request);
            final var responseDto = service.find(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }

    }



    public <T> T read(Class<T> clazz, HttpServletRequest request) {
        for (final var converter : converters) {
            if (!converter.canRead(request.getContentType(), clazz)) {
                continue;
            }

            try {
                return converter.read(request.getReader(), clazz);
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: convert to special exception
                throw new RuntimeException(e);
            }
        }
        // TODO: convert to special exception
        throw new RuntimeException("no converters support given content type");
    }

    private void write(Object data, String contentType, HttpServletResponse response) {
        for (final var converter : converters) {
            if (!converter.canWrite(contentType, data.getClass())) {
                continue;
            }

            try {
                response.setContentType(contentType);
                converter.write(response.getWriter(), data);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: convert to special exception
                throw new RuntimeException(e);
            }
        }
        // TODO: convert to special exception
        throw new RuntimeException("no converters support given content type");
    }

}
