package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class UserInfoRequestDto {
  String firstName;
  String secondName;
  String description;
}
