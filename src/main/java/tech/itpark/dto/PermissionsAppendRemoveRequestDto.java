package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class PermissionsAppendRemoveRequestDto {
  String operation;
  Set<String> roles;
}
