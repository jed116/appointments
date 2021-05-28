package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UserRolesGetResponseDto {
  Set<String> active;
  Set<String> inactive;
}
