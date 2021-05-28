package tech.itpark.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import tech.itpark.bodyconverter.BodyConverter;
import tech.itpark.dto.*;
import tech.itpark.http.ContentTypes;
import tech.itpark.security.HttpServletRequestAuthToken;
import tech.itpark.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class UserController {
  private final UserService service;
  private final List<BodyConverter> converters;


  public void register(HttpServletRequest request, HttpServletResponse response){
    try {
      final var requestDto = read(UserRegisterRequestDto.class, request);
      final var responseDto = service.register(requestDto);
      write(responseDto, ContentTypes.APPLICATION_JSON, response);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.setStatus(400);
      write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
    }
  }

  public void delete(HttpServletRequest request, HttpServletResponse response) {
    final var token = HttpServletRequestAuthToken.token(request);
    final var requestDto = read(UnregisterRequestDto.class, request);
    final var responseDto = service.delete(requestDto, token);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  public void restore(HttpServletRequest request, HttpServletResponse response) {
    final var requestDto = read(RestoreRequestDto.class, request);
    final var responseDto = service.restore(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }



  public void login(HttpServletRequest request, HttpServletResponse response){
    try {
      final var requestDto = read(LoginRequestDto.class, request);
      final var responseDto = service.login(requestDto);
      write(responseDto, ContentTypes.APPLICATION_JSON, response);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.setStatus(401); //401=Unauthorized
      write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
    }

  }

  public void logout(HttpServletRequest request, HttpServletResponse response) {
    try {
      final var token = HttpServletRequestAuthToken.token(request);
      final var auth = HttpServletRequestAuthToken.auth(request);
      final var responseDto = service.logout(auth, token);
      write(responseDto, ContentTypes.APPLICATION_JSON, response);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.setStatus(401); //401=Unauthorized
      write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
    }
  }


  public void updatePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
    write(service.updatePassword(read(UpdatePasswordRequestDto.class, request)), ContentTypes.APPLICATION_JSON, response);
  }

  public void updateSecret(HttpServletRequest request, HttpServletResponse response) throws IOException {
    write(service.updateSecret(read(UpdateSecretRequestDto.class, request)), ContentTypes.APPLICATION_JSON, response);
  }


  public void getUsersByRoles(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      final var auth = HttpServletRequestAuthToken.auth(request);
      final var requestDto = read(UsersByRolesRequestDto.class, request);
      final var responseDto = service.getUsersByRoles(requestDto, auth);
      write(responseDto, ContentTypes.APPLICATION_JSON, response);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.setStatus(403); //403=Forbidden
      write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
    }

  }

  public void findUsers(HttpServletRequest request, HttpServletResponse response) {
    final var requestDto = read(UsersFindRequestDto.class, request);
    final var responseDto = service.findUsers(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  public void findUsers_Doctors(HttpServletRequest request, HttpServletResponse response) {
    final var requestDto = read(UsersFindRequestDto.class, request);
    final var responseDto = service.findUsers_Doctors(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  public void findUsers_Patients(HttpServletRequest request, HttpServletResponse response) {
    final var requestDto = read(UsersFindRequestDto.class, request);
    final var responseDto = service.findUsers_Patients(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  public void findUsers_Chiefs(HttpServletRequest request, HttpServletResponse response) {
    final var requestDto = read(UsersFindRequestDto.class, request);
    final var responseDto = service.findUsers_Chiefs(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

////////////////////////////////////////////////////////////////////////////////////////////////////////////// R O L E S

  public void getUserRoles(HttpServletRequest request, HttpServletResponse response) {
    try {
      final var auth = HttpServletRequestAuthToken.auth(request);
      final var requestDto = read(UserRolesGetRequestDto.class, request);
      final var responseDto = service.getUserRoles(requestDto, auth);
      write(responseDto, ContentTypes.APPLICATION_JSON, response);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.setStatus(403); //403=Forbidden
      write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
    }
  }

  public void activeUserRoles(HttpServletRequest request, HttpServletResponse response) {
    try {
      final var auth = HttpServletRequestAuthToken.auth(request);
      final var requestDto = read(UserRolesActiveRequestDto.class, request);
      final var responseDto = service.activateUserRoles(requestDto, auth);
      write(responseDto, ContentTypes.APPLICATION_JSON, response);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.setStatus(403); //403=Forbidden
      write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
    }
  }

  public void appendUserRoles(HttpServletRequest request, HttpServletResponse response) {
    try {
      final var auth = HttpServletRequestAuthToken.auth(request);
      final var requestDto = read(UserRolesAppendRemoveRequestDto.class, request);
      final var responseDto = service.appendUserRoles(requestDto, auth);
      write(responseDto, ContentTypes.APPLICATION_JSON, response);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.setStatus(403); //403=Forbidden
      write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
    }
  }

  public void removeUserRoles(HttpServletRequest request, HttpServletResponse response) {
    try {
      final var auth = HttpServletRequestAuthToken.auth(request);
      final var requestDto = read(UserRolesAppendRemoveRequestDto.class, request);
      final var responseDto = service.removeUserRoles(requestDto, auth);
      write(responseDto, ContentTypes.APPLICATION_JSON, response);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.setStatus(403); //403=Forbidden
      write(new ErrorResponseDto(e.getMessage()), ContentTypes.APPLICATION_JSON, response);
    }

  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////// I N F O

  public void setUserInfo(HttpServletRequest request, HttpServletResponse response) {
    final var auth = HttpServletRequestAuthToken.auth(request);
    final var requestDto = read(UserInfoRequestDto.class, request);
    final var responseDto = service.setUserInfo(requestDto, auth);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  public void getUserInfo(HttpServletRequest request, HttpServletResponse response){
    final var token = HttpServletRequestAuthToken.token(request);
    final var responseDto = service.getUserInfo(token);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
