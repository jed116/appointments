package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UserRolesAppendRemoveRequestDto {
  String login;
  Set<String> roles;
}
