package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UserRolesActiveRequestDto {
  String login;
  Set<String> roles;
  boolean active;
}
