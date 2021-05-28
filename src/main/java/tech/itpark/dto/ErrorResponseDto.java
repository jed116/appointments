package tech.itpark.dto;

import lombok.AllArgsConstructor;
import lombok.Value;
import tech.itpark.model.User;

import java.util.List;

@AllArgsConstructor
public class ErrorResponseDto {
  String message;
}
