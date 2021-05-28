package tech.itpark.dto;

import lombok.Value;

import java.util.Map;

@Value
public class RolesFindResponseDto {
  Map<String, Map<String, Boolean>> roles;
}
