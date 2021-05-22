package tech.itpark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegistrationRequestDto {
  private String login;
  private String password;
  private String secret;
  private Set<String> roles;
//  private String[] roles;
}
