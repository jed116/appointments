package tech.itpark.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.itpark.security.Auth;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User implements Auth {
  private long    id;
  private String  login;
  private String  password; // FIXME: желательно не таскать соль + хеш пароля (по всему приложению)
  private String  secret;
  private Boolean  removed;

  private Set<String> roles;

  private String  firstName;
  private String  secondName;
  private String  description;

}
