package tech.itpark.dto;

import lombok.Value;
import tech.itpark.model.User;

import java.util.List;

@Value
public class UsersFindResponseDto {
  List<User> users;
}
