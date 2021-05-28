package tech.itpark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.itpark.configuration.AppParams;
import tech.itpark.crypto.PasswordHasher;
import tech.itpark.crypto.TokenGenerator;
import tech.itpark.dto.*;
import tech.itpark.exception.AuthErrorException;
import tech.itpark.exception.PermissionDeniedException;
import tech.itpark.model.TokenAuth;
import tech.itpark.model.User;
import tech.itpark.repository.UserRepository;
import tech.itpark.security.AuthProvider;
import tech.itpark.security.Auth;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements AuthProvider {
  private final UserRepository repository;
  private final PasswordHasher passwordHasher;
  private final TokenGenerator tokenGenerator;

  @Override
  public Auth provide(String token) {
    // BL -> Optional.empty -> Anonymous
    return repository.getByToken(token)
            .map(o -> (Auth) o)
            .orElse(Auth.anonymous())
            ;
  }



  public UserRegisterResponseDto register(UserRegisterRequestDto request) {
    if (request.getLogin() == null) {
      throw new RuntimeException("login can't be null");
    }

    if (!request.getLogin().matches("^[a-z0-9]{5,10}$")) {
      throw new RuntimeException("bad login");
    }

    if (request.getPassword() == null) {
      throw new RuntimeException("password can't be null");
    }

    if (request.getPassword().length() < 5) {
      throw new RuntimeException("minimal length of password must be greater than 5");
    }

    if (request.getSecret() == null) {
      throw new RuntimeException("secret can't be null");
    }

    if (request.getSecret().length() < 5) {
      throw new RuntimeException("minimal length of secret must be greater than 5");
    }

    final var user = repository.getByLogin(request.getLogin());
    if(user.isPresent()){
      throw new RuntimeException("User with login '" + user.get().getLogin() + "' already exist!!!");
    }

    final var registerUserRoles = request.getRoles();
    final var roleAttributes = AppParams.getRoleAttributes();
    final var unknownRolesForUser = registerUserRoles.stream().filter(r -> !roleAttributes.containsKey(r)).collect(Collectors.toSet());
    if (unknownRolesForUser.size() > 0){
      throw new RuntimeException("UNKNOWN ROLES: " + String.join(", ", unknownRolesForUser) + " !!!");
    }

    final var passwordHash = passwordHasher.hash(request.getPassword());
    final var secretHash = passwordHasher.hash(request.getSecret());

    // register
    final var registerUser = repository.save(
        new User(0, request.getLogin(), passwordHash, secretHash, false, registerUserRoles, "", "", "")
    );
    long saved_id = registerUser.getId();
    if (saved_id != 0){
      repository.appendUserRoles(registerUser, registerUser.getRoles());
    }

    return new UserRegisterResponseDto(registerUser.getId());
  }

  public UnregisterResponseDto delete(UnregisterRequestDto requestDto, String token) {
    final var user = repository.getByToken(token)
            .orElseThrow(() -> new RuntimeException("user not found"));

    if (!passwordHasher.matches(user.getPassword(), requestDto.getPassword())) {
      throw new RuntimeException("passwords not match");
    }
    repository.remove(user, true);
    return new UnregisterResponseDto(user.getId());
  }

  public RestoreResponseDto restore(RestoreRequestDto requestDto) {
    final var user = repository.getByLogin(requestDto.getLogin())
            .orElseThrow(() -> new RuntimeException("User not found!"));

    if (!passwordHasher.matches(user.getPassword(), requestDto.getPassword())) {
      throw new AuthErrorException("Passwords not match!");
    }

    if (!passwordHasher.matches(user.getSecret(), requestDto.getSecret())) {
      throw new AuthErrorException("Secret not match!");
    }

    repository.remove(user, false);
    return new RestoreResponseDto(user.getId());
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public LoginResponseDto login(LoginRequestDto request) {
    final var user = repository.getByLogin(request.getLogin())
        .orElseThrow(() -> new AuthErrorException("User not found!"));

    if (!passwordHasher.matches(user.getPassword(), request.getPassword())) {
      throw new AuthErrorException("Passwords not match!");
    }

    if (user.getRemoved()){
      throw new AuthErrorException("Users deleted!");
    }

    final var token = tokenGenerator.generate();
    repository.saveToken(new TokenAuth(user.getId(), token));
    return new LoginResponseDto(token);
  }

  public LogoutResponseDto logout(Auth auth, String token) {
    User user = repository.getByToken(token).orElseThrow(() -> new RuntimeException("Wrong token!"));
    long id = auth.getId();
    if (id != user.getId()){
      throw new AuthErrorException("Wrong authorization !"); // it's impossible
    }
    repository.deleteToken(new TokenAuth(id, token));
    return new LogoutResponseDto(id);
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public UpdatePasswordResponseDto updatePassword(UpdatePasswordRequestDto requestDto) {
    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("wrong login!"));
    if (!passwordHasher.matches(user.getSecret(), requestDto.getSecret())){
      throw new PermissionDeniedException("wrong secret!");
    }
    user.setPassword(passwordHasher.hash(requestDto.getPassword()));
    repository.updatePassword(user);
    return new UpdatePasswordResponseDto(user.getId());
  }

  public UpdateSecretResponseDto updateSecret(UpdateSecretRequestDto requestDto) {
    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("wrong login!"));
    if (!passwordHasher.matches(user.getPassword(), requestDto.getPassword())){
      throw new PermissionDeniedException("wrong password!");
    }
    user.setSecret(passwordHasher.hash(requestDto.getSecret()));
    repository.updateSecret(user);
    return new UpdateSecretResponseDto(user.getId());
  }


  public UsersByRolesResponseDto getUsersByRoles(UsersByRolesRequestDto requestDto, Auth auth){
    long user_id = auth.getId();
    if (user_id <= 0){
      throw new RuntimeException("USER NOT AUTHORIZED !!!");
    }
    User user = (User) auth;
    final var userRoles = user.getRoles();
    boolean isAdmin = AppParams.isAdmin(userRoles);
    if (!isAdmin){
      throw new PermissionDeniedException("ADMIN OPERATION !!!");
    }

    return new UsersByRolesResponseDto(repository.getUsersByRoles(requestDto.getRoles(), requestDto.getActive()));
  }

////////////////////////////////////////////////////////////////////////////////////////////////////////////// R O L E S

  public UserRolesGetResponseDto getUserRoles(UserRolesGetRequestDto requestDto, Auth auth) {
    long user_id = auth.getId();
    if (user_id <= 0){
      throw new RuntimeException("USER NOT AUTHORIZED !!!");
    }

    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("WRONG LOGIN!"));
    final var userRolesActive = repository.getUserRoles(user, 1);
    final var userRolesInActive = repository.getUserRoles(user, -1);

    return new UserRolesGetResponseDto(userRolesActive, userRolesInActive);
  }

  public UserRolesAppendRemoveResponseDto appendUserRoles(UserRolesAppendRemoveRequestDto requestDto, Auth auth) {
    long user_id = auth.getId();
    if (user_id <= 0){
      throw new RuntimeException("USER NOT AUTHORIZED !!!");
    }

    final var rolesToAppend = requestDto.getRoles();
    final var unknownRolesToAppend = AppParams.filterUnknownRoles(rolesToAppend);
    if (unknownRolesToAppend.size() > 0){
      throw new RuntimeException("UNKNOWN ROLES: " + String.join(", ", unknownRolesToAppend) + " !!!");
    }
    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("WRONG LOGIN!"));
    final var userRoles = repository.getUserRoles(user, 0);
    final var assignedUserRoles = rolesToAppend.stream().filter(r -> userRoles.contains(r)).collect(Collectors.toSet());
    if (assignedUserRoles.size() > 0){
      throw new RuntimeException("The user '" + user.getLogin() + "' has already been assigned the following roles: " + String.join(", ", assignedUserRoles) + " !!!");
    }

    repository.appendUserRoles(user, rolesToAppend);

    return new UserRolesAppendRemoveResponseDto(user.getId());
  }

  public UserRolesAppendRemoveResponseDto removeUserRoles(UserRolesAppendRemoveRequestDto requestDto, Auth auth) {

    final var rolesToDelete = requestDto.getRoles();
    final var unknownRolesToDelete = AppParams.filterUnknownRoles(rolesToDelete);
    if (unknownRolesToDelete.size() > 0){
      throw new RuntimeException("UNKNOWN ROLES: " + String.join(", ", unknownRolesToDelete) + " !!!");
    }
    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("WRONG LOGIN!"));
    final var userRoles = repository.getUserRoles(user, 0);
    final var notAssignedUserRoles = rolesToDelete.stream().filter(r -> !userRoles.contains(r)).collect(Collectors.toSet());
    if (notAssignedUserRoles.size() > 0){
      throw new RuntimeException("The user '" + user.getLogin() + "' was not assigned the following roles: " + String.join(", ", notAssignedUserRoles) + " !!!");
    }

    repository.removeUserRoles(user, rolesToDelete);

    return new UserRolesAppendRemoveResponseDto(user.getId());
  }

  public UserRolesActiveResponseDto activateUserRoles(UserRolesActiveRequestDto requestDto, Auth auth) {
    long user_id = auth.getId();
    if (user_id <= 0){
      throw new RuntimeException("USER NOT AUTHORIZED !!!");
    }

    final var rolesForUser = requestDto.getRoles();
    final var roleAttributes = AppParams.getRoleAttributes();
    final var unknownRolesForUser = rolesForUser.stream().filter(r -> !roleAttributes.containsKey(r)).collect(Collectors.toSet());
    if (unknownRolesForUser.size() > 0){
      throw new RuntimeException("UNKNOWN ROLES: " + String.join(", ", unknownRolesForUser) + " !!!");
    }

    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("WRONG LOGIN!"));
    final var userRoles = repository.getUserRoles(user, 0);
    final var notUserRoles = rolesForUser.stream().filter(r -> !userRoles.contains(r)).collect(Collectors.toSet());
    if (notUserRoles.size() > 0){
      throw new RuntimeException("USER " +user.getLogin() + " IS NOT ASSIGNED FOLLOWING ROLE: " + String.join(", ", notUserRoles) + " !!!");
    }

    repository.activeUserRoles(user.getId(), rolesForUser, requestDto.isActive());
    return new UserRolesActiveResponseDto(user.getId());
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////  I N F O

  public UserInfoResponseDto setUserInfo(UserInfoRequestDto requestDto, Auth auth) {
    long id = auth.getId();
    repository.setUserInfo(id, requestDto.getFirstName(), requestDto.getSecondName(), requestDto.getDescription());
    return new UserInfoResponseDto(id);
  }

  public GetUserInfoResponseDto getUserInfo(String token) {
    User user = repository.getByToken(token).orElseThrow(() -> new RuntimeException("Wrong token!"));
    return new GetUserInfoResponseDto(user.getId(),
                                      user.getLogin(),
                                      user.getFirstName(),
                                      user.getSecondName(),
                                      user.getDescription(),
                                      user.getRoles());
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////  F I N D

  public UsersByRolesResponseDto findUsers(UsersFindRequestDto requestDto) {
    return new UsersByRolesResponseDto(repository.findUsers(requestDto.getRoles(), requestDto.getInfo()));
  }

  public UsersByRolesResponseDto findUsers_Patients(UsersFindRequestDto requestDto) {
    final var roles = AppParams.rolesPatient();
    return new UsersByRolesResponseDto(repository.findUsers(roles, requestDto.getInfo()));
  }

  public UsersByRolesResponseDto findUsers_Doctors(UsersFindRequestDto requestDto) {
    final var roles = AppParams.rolesDoctor();
    return new UsersByRolesResponseDto(repository.findUsers(roles, requestDto.getInfo()));
  }

  public UsersByRolesResponseDto findUsers_Chiefs(UsersFindRequestDto requestDto) {
    final var roles = AppParams.rolesChief();
    return new UsersByRolesResponseDto(repository.findUsers(roles, requestDto.getInfo()));
  }

}
