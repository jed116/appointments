package tech.itpark.dto;

import lombok.Value;

import java.util.Map;

@Value
public class RolesAppendRequestDto {
  Map<String, ?> attributes;
}
