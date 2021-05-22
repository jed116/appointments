package tech.itpark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
public class UnregisterRequestDto {
  String password;
}
