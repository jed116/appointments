package tech.itpark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Set;

//@NoArgsConstructor
//@AllArgsConstructor
//@Data
@Value
public class UserRegisterRequestDto {
  private String login;
  private String password;
  private String secret;
  private Set<String> roles;
}
