package tech.itpark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import tech.itpark.security.Roles;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements AuthProvider {
  private final UserRepository repository;
  private final PasswordHasher passwordHasher;
  private final TokenGenerator tokenGenerator;

//  public Optional<User> getById() {
//    return Optional.empty();
//  }
//
//  public User save(User user) {
//    return user;
//  }

  @Override
  public Auth provide(String token) {
    // BL -> Optional.empty -> Anonymous
    return repository.getByToken(token)
            .map(o -> (Auth) o)
            .orElse(Auth.anonymous())
            ;
  }

  public RegistrationResponseDto register(RegistrationRequestDto request) {
    // --1. Свободен ли логин
    // 2. Длина пароля и т.д. и т.п.
    // 3. Хеш пароля
    // -> 4. Сохранение в БД
    // TODO: Чистота данных
    // TODO: " admin ", ADMIN, aDmIN
    // TODO: sanitizing (очистка данных) <- bad idea
    // TODO: pattern matching (whitelist/allowlist)
    // TODO: abcdef...0-9 (best practice)
    // Regexp:
    // TODO: https://regex101.com/
    // TODO: ^ смотрим с начала строки
    // TODO: $ смотрим до конца строки
    // TODO: ^admin$
    // TODO: [abc...zA...Z0...9]
    // TODO: [a-zA-Z0-9]
    // TODO: квантификаторы:
    // TODO: ? - 0-1 символ
    // TODO: * - 0+ символ
    // TODO: + - 1+ символ
    // TODO: {min}, {min, max}
    if (request.getLogin() == null) {
      throw new RuntimeException("login can't be null");
    }

    if (!request.getLogin().matches("^[a-z0-9]{5,10}$")) {
      throw new RuntimeException("bad login");
//      throw new BadLoginException();
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

    final var passwordHash = passwordHasher.hash(request.getPassword());
    final var secretHash = passwordHasher.hash(request.getSecret());

    // register
    final var saved = repository.save(
        new User(0, request.getLogin(), passwordHash, secretHash, false, request.getRoles(), "", "", "")
    );

    long saved_id = saved.getId();
    if (saved_id != 0){
      repository.saveRoles(saved_id, saved.getRoles());
      if (saved.getRoles().contains(Roles.ROLE_PATIENT)){
        repository.updateRoles(saved_id, Set.of(Roles.ROLE_PATIENT), true  );
      }
    }

    return new RegistrationResponseDto(saved.getId());
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


  public UsersResponseDto getUsersByRoles(UserRolesRequestDto  requestDto){
    return new UsersResponseDto(repository.getUserRoles(requestDto.getRoles(), requestDto.getActive()));
  }

  public UserRolesActiveResponseDto activateUsersRoles(UserRolesActiveRequestDto requestDto) {
    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("wrong login!"));
    repository.activeUserRoles(user.getId(), requestDto.getRoles(), requestDto.isActive());
    return new UserRolesActiveResponseDto(user.getId());
  }

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




  public UsersResponseDto findUsers(UsersRequestDto requestDto) {
    return new UsersResponseDto(repository.findUsers(requestDto.getRoles(), requestDto.getInfo()));
  }

  public UsersResponseDto findUsers_Doctors(UsersRequestDto requestDto) {
    return new UsersResponseDto(repository.findUsers(Set.of(Roles.ROLE_DOCTOR), requestDto.getInfo()));
  }

  public UsersResponseDto findUsers_Patients(UsersRequestDto requestDto) {
    return new UsersResponseDto(repository.findUsers(Set.of(Roles.ROLE_PATIENT), requestDto.getInfo()));
  }


}
