package tech.itpark.dto;

import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
public class PermissionsAppendRemoveResponseDto {
  String operation;
  Set<String> roles;
}
