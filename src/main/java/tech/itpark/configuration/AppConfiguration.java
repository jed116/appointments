package tech.itpark.configuration;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import tech.itpark.controller.AppointmentController;
import tech.itpark.controller.RolePermissionController;
import tech.itpark.controller.UserController;
import tech.itpark.bodyconverter.BodyConverter;
import tech.itpark.bodyconverter.GsonBodyConverter;
import tech.itpark.crypto.PasswordHasher;
import tech.itpark.crypto.PasswordHasherDefaultImpl;
import tech.itpark.crypto.TokenGenerator;
import tech.itpark.crypto.TokenGeneratorDefaultImpl;
import tech.itpark.http.Handler;

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
@PropertySource("classpath:/app.properties")
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
    return List.of(new GsonBodyConverter(new Gson()));
  }

  @Bean
  public Map<String, Handler> routesPost(UserController userCtrl, AppointmentController appointCtrl, RolePermissionController rpCtrl) {

    AppParams.init(ROLE_ANONYMOUS, PROP_ROLES_NAME, PROP_ROLES_ACTIVE, ATTR_ROLES_PATIENT, ATTR_ROLES_DOCTOR, ATTR_ROLES_CHIEF, ATTR_ROLES_ADMIN);

    AppParams.setRoleAttributes(rpCtrl.initRoleAttributes());

    appointCtrl.initParams(MIN_APPOINTMENT_TIME, START_APPOINTMENT_PERIOD, END_APPOINTMENT_PERIOD, APPOINTMENT_DAY_LIMIT);

    return Map.ofEntries(
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/register",      userCtrl::register),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/delete",        userCtrl::delete),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/restore",       userCtrl::restore),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/login",         userCtrl::login),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/logout",        userCtrl::logout),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/password",      userCtrl::updatePassword),
              new AbstractMap.SimpleEntry<String, Handler>("/api/auth/secret",        userCtrl::updateSecret),

              new AbstractMap.SimpleEntry<String, Handler>("/api/users/find",         userCtrl::findUsers),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/find/doctors", userCtrl::findUsers_Doctors),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/find/patients",userCtrl::findUsers_Patients),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/find/chiefs",  userCtrl::findUsers_Chiefs),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/roles",        userCtrl::getUsersByRoles),    //*

              new AbstractMap.SimpleEntry<String, Handler>("/api/users/info/get",     userCtrl::getUserInfo),
              new AbstractMap.SimpleEntry<String, Handler>("/api/users/info/set",     userCtrl::setUserInfo),

              new AbstractMap.SimpleEntry<String, Handler>("/api/user/roles/get",     userCtrl::getUserRoles),
              new AbstractMap.SimpleEntry<String, Handler>("/api/user/roles/append",  userCtrl::appendUserRoles),
              new AbstractMap.SimpleEntry<String, Handler>("/api/user/roles/remove",  userCtrl::removeUserRoles),
              new AbstractMap.SimpleEntry<String, Handler>("/api/user/roles/active",  userCtrl::activeUserRoles),

              new AbstractMap.SimpleEntry<String, Handler>("/api/appointment/open",   appointCtrl::open),
              new AbstractMap.SimpleEntry<String, Handler>("/api/appointment/book",   appointCtrl::book),
              new AbstractMap.SimpleEntry<String, Handler>("/api/appointment/unbook", appointCtrl::unbook),
              new AbstractMap.SimpleEntry<String, Handler>("/api/appointment/close",  appointCtrl::close),
              new AbstractMap.SimpleEntry<String, Handler>("/api/appointment/cancel", appointCtrl::cancel),
              new AbstractMap.SimpleEntry<String, Handler>("/api/appointment/find",   appointCtrl::find),

              new AbstractMap.SimpleEntry<String, Handler>("/api/permission/find",    rpCtrl::findPermissions),
              new AbstractMap.SimpleEntry<String, Handler>("/api/permission/append",  rpCtrl::appendPermissions),
              new AbstractMap.SimpleEntry<String, Handler>("/api/permission/remove",  rpCtrl::removePermissions),

              new AbstractMap.SimpleEntry<String, Handler>("/api/roles/find",          rpCtrl::findRoles),
              new AbstractMap.SimpleEntry<String, Handler>("/api/roles/append",        rpCtrl::appendRoles),
              new AbstractMap.SimpleEntry<String, Handler>("/api/roles/remove",        rpCtrl::removeRoles)

      );
  }

  @Bean
  public Map<String, Set<String>> rolePermissions(RolePermissionController controller) {
    return controller.initRolePermissions();
  }


  @Value("${role_anonymous:ROLE_ANONYMOUSe}")
  public String ROLE_ANONYMOUS;

  @Value("${prop_roles_name}")
  public String PROP_ROLES_NAME;

  @Value("${prop_roles_active}")
  public String PROP_ROLES_ACTIVE;

  @Value("${attr_roles_admin}")
  public String ATTR_ROLES_ADMIN;

  @Value("${attr_roles_chief}")
  public String ATTR_ROLES_CHIEF;

  @Value("${attr_roles_doctor}")
  public String ATTR_ROLES_DOCTOR;

  @Value("${attr_roles_patient}")
  public String ATTR_ROLES_PATIENT;

  @Value("${minimal_appointment_time:1199}")
  public long MIN_APPOINTMENT_TIME;

  @Value("${start_appointment_period:0}")
  public long START_APPOINTMENT_PERIOD;

  @Value("${end_appointment_period:7}")
  public long END_APPOINTMENT_PERIOD;

  @Value("${appointment_day_limit:1}")
  public long APPOINTMENT_DAY_LIMIT;

}
