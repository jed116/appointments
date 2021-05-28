package tech.itpark.dto;

import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
public class RolesFindRequestDto {
  Map<String, Boolean> attributes;
}
