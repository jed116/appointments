package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UserRolesActiveRequestDto {
  private String login;
  private Set<String> roles;
  private boolean active;
}
