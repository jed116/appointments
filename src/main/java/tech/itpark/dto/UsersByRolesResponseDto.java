package tech.itpark.dto;

import lombok.Value;
import tech.itpark.model.User;

import java.util.List;
import java.util.Set;

@Value
public class UsersByRolesResponseDto {
  List<User> users;
}
