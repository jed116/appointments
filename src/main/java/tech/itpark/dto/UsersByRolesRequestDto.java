package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UsersByRolesRequestDto {
  Set<String> roles;
  int active;
}
