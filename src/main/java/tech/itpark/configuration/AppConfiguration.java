package tech.itpark.configuration;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.itpark.controller.UserController;
import tech.itpark.bodyconverter.BodyConverter;
import tech.itpark.bodyconverter.GsonBodyConverter;
import tech.itpark.crypto.PasswordHasher;
import tech.itpark.crypto.PasswordHasherDefaultImpl;
import tech.itpark.crypto.TokenGenerator;
import tech.itpark.crypto.TokenGeneratorDefaultImpl;
import tech.itpark.http.Handler;
import tech.itpark.security.Roles;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class AppConfiguration {
  @Bean
  public DataSource dataSource() throws NamingException {
    // Spring way:
    // return (DataSource) new JndiTemplate().lookup("java:/comp/env/jdbc/db");
    final var cxt = new InitialContext();
    return (DataSource) cxt.lookup("java:/comp/env/jdbc/db");
  }

  @Bean
  public PasswordHasher passwordHasher(MessageDigest digest) {
    return new PasswordHasherDefaultImpl(digest);
  }

  @Bean
  public MessageDigest messageDigest() throws NoSuchAlgorithmException {
    return MessageDigest.getInstance("SHA-256");
  }

  @Bean
  public TokenGenerator tokenGenerator() {
    return new TokenGeneratorDefaultImpl();
  }

  @Bean
  public List<BodyConverter> bodyConverters() {
    return List.of(
        new GsonBodyConverter(new Gson())
    );
  }

  @Bean
  public Map<String, Handler> routesPost(UserController controller) {
    return Map.ofEntries(
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/register",      controller::register),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/delete",        controller::delete),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/restore",       controller::restore),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/login",         controller::login),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/logout",        controller::logout),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/password",      controller::updatePassword),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/secret",        controller::updateSecret),

              new AbstractMap.SimpleEntry<String, Handler>("/api/users/find",          controller::findUsers),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/find_doctors",  controller::findUsers_Doctors),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/find_patients", controller::findUsers_Patients),

              new AbstractMap.SimpleEntry<String, Handler>("/api/users/info/get",     controller::getUserInfo),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/info/set",     controller::setUserInfo),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/roles",        controller::getUserRoles),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/roles/active", controller::activeUserRoles)
      );
  }

  @Bean
  public Map<String, Set<String>> queriesPermissions() {
      return Map.ofEntries(
         new AbstractMap.SimpleEntry<>("/api/auth/delete",  Set.of(Roles.ROLE_CHEIF, Roles.ROLE_DOCTOR, Roles.ROLE_PATIENT)),
//         new AbstractMap.SimpleEntry<>("/api/auth/restore", Set.of(Roles.ROLE_ADMIN)),

         new AbstractMap.SimpleEntry<>("/api/users/roles",        Set.of(Roles.ROLE_ADMIN)),
         new AbstractMap.SimpleEntry<>("/api/users/roles/active", Set.of(Roles.ROLE_ADMIN)),

         new AbstractMap.SimpleEntry<>("/api/users/get",          Set.of(Roles.ROLE_ADMIN)),
         new AbstractMap.SimpleEntry<>("/api/users/get_doctors",  Set.of(Roles.ROLE_ADMIN, Roles.ROLE_CHEIF, Roles.ROLE_PATIENT)),
         new AbstractMap.SimpleEntry<>("/api/users/get_patients", Set.of(Roles.ROLE_ADMIN, Roles.ROLE_CHEIF, Roles.ROLE_DOCTOR)),

         new AbstractMap.SimpleEntry<>("/api/users/info/set",     Set.of(Roles.ROLE_PATIENT, Roles.ROLE_DOCTOR, Roles.ROLE_CHEIF))
      );
  }

//  @Bean // DI by interface
//  public List<Middleware> middlewares(AuthProvider provider) {
//    return List.of(
//        new TokenExtractorFromAuthorizationHeader(),
//        new AuthFromTokenExtractor(provider)
//    );
//  }
}
