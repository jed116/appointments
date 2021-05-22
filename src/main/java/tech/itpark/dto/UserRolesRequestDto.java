package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UserRolesRequestDto {
  Set<String> roles;
  int active;
}
