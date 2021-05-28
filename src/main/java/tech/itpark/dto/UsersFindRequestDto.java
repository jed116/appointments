package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UsersFindRequestDto {
  Set<String> roles;
  Set<String> info;
}
