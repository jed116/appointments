package tech.itpark.dto;

import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
public class PermissionsFindRequestDto {
  Set<String> operations;
  Set<String> roles;
}
