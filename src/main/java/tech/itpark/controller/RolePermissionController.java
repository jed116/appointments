package tech.itpark.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import tech.itpark.bodyconverter.BodyConverter;
import tech.itpark.configuration.AppParams;
import tech.itpark.dto.*;
import tech.itpark.exception.AuthErrorException;
import tech.itpark.exception.BadRequestException;
import tech.itpark.exception.DataAccessException;
import tech.itpark.exception.PermissionDeniedException;
import tech.itpark.http.ContentTypes;
import tech.itpark.security.HttpServletRequestAuthToken;
import tech.itpark.service.RolePermissionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class RolePermissionController {
    private final RolePermissionService service;
    private final List<BodyConverter> converters;

    public Map<String, Map<String, Boolean>> initRoleAttributes() {
        return  service.initRoleAttributes();
    }

    public Map<String, Set<String>> initRolePermissions(){
        return service.initRolePermissions();
    }

    private void updateServletPermissions(HttpServletRequest request){
        request.getServletContext().setAttribute("PERMISSIONS", initRolePermissions());
    }

/////////////////////////////////////////////////////////////////////////////////////////////////  P E R M I S S I O N S

    public void findPermissions(HttpServletRequest request, HttpServletResponse response) {
        try {
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(PermissionsFindRequestDto.class, request);
            final var responseDto = service.findPermissions(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);

        } catch (PermissionDeniedException e){
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (AuthErrorException e){
            e.printStackTrace();
            response.setStatus(401); //Unauthorized
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (BadRequestException e){
            e.printStackTrace();
            response.setStatus(400); //Bad request
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (DataAccessException e){
            e.printStackTrace();
            response.setStatus(500); //Internal Server Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(502); //Unknown Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

    public void appendPermissions(HttpServletRequest request, HttpServletResponse response) {
        try {
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(PermissionsAppendRemoveRequestDto.class, request);
            final var responseDto = service.appendPermissions(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
            updateServletPermissions(request);
        } catch (PermissionDeniedException e){
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (AuthErrorException e){
            e.printStackTrace();
            response.setStatus(401); //Unauthorized
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (BadRequestException e){
            e.printStackTrace();
            response.setStatus(400); //Bad request
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);

        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(520); //Unknown Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

    public void removePermissions(HttpServletRequest request, HttpServletResponse response) {
        try {
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(PermissionsAppendRemoveRequestDto.class, request);
            final var responseDto = service.removePermissions(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
            updateServletPermissions(request);
        } catch (PermissionDeniedException e){
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (AuthErrorException e){
            e.printStackTrace();
            response.setStatus(401); //Unauthorized
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (BadRequestException e){
            e.printStackTrace();
            response.setStatus(400); //Bad request
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (DataAccessException e){
            e.printStackTrace();
            response.setStatus(500); //Internal Server Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(520); //Unknown Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////  R O L E S

    public void findRoles(HttpServletRequest request, HttpServletResponse response) {
        try {
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(RolesFindRequestDto.class, request);
            final var responseDto = service.findRoles(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);

        } catch (PermissionDeniedException e){
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (AuthErrorException e){
            e.printStackTrace();
            response.setStatus(401); //Unauthorized
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (BadRequestException e){
            e.printStackTrace();
            response.setStatus(400); //Bad request
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (DataAccessException e){
            e.printStackTrace();
            response.setStatus(500); //Internal Server Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(502); //Unknown Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

    public void appendRoles(HttpServletRequest request, HttpServletResponse response) {
        try {
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(RolesAppendRequestDto.class, request);
            final var responseDto = service.appendRole(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
            AppParams.setRoleAttributes(initRoleAttributes());
        } catch (PermissionDeniedException e){
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (AuthErrorException e){
            e.printStackTrace();
            response.setStatus(401); //Unauthorized
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (BadRequestException e){
            e.printStackTrace();
            response.setStatus(400); //Bad request
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (DataAccessException e){
            e.printStackTrace();
            response.setStatus(500); //Internal Server Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(502); //Unknown Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

    public void removeRoles(HttpServletRequest request, HttpServletResponse response) {
        try {
            final var auth = HttpServletRequestAuthToken.auth(request);
            final var requestDto = read(RolesRemoveRequestDto.class, request);
            final var responseDto = service.removeRole(requestDto, auth);
            write(responseDto, ContentTypes.APPLICATION_JSON, response);
            AppParams.setRoleAttributes(initRoleAttributes());
        } catch (PermissionDeniedException e){
            e.printStackTrace();
            response.setStatus(403); //Forbidden
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (AuthErrorException e){
            e.printStackTrace();
            response.setStatus(401); //Unauthorized
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
        catch (BadRequestException e) {
            e.printStackTrace();
            response.setStatus(400); //Bad request
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (DataAccessException e){
            e.printStackTrace();
            response.setStatus(500); //Internal Server Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }catch (RuntimeException e) {
            e.printStackTrace();
            response.setStatus(502); //Unknown Error
            write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
        }
    }

/////////////////////////////////////////////////////////////////////////////////////////////////  P E R M I S S I O N S

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
