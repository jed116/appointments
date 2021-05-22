package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UsersRequestDto {
  Set<String> roles;
  Set<String> info;
}
