package tech.itpark.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tech.itpark.bodyconverter.BodyConverter;
import tech.itpark.dto.AppointCreateRequestDto;
import tech.itpark.http.ContentTypes;
import tech.itpark.service.AppointmentService;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService service;
    private final List<BodyConverter> converters;

    public void create(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final var requestDto = read(AppointCreateRequestDto.class, request);
        final var responseDto = service.create(requestDto);
        write(responseDto, ContentTypes.APPLICATION_JSON, response);
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
