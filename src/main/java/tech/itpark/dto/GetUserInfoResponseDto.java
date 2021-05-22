package tech.itpark.dto;

import lombok.Value;

import java.util.Set;

@Value
public class GetUserInfoResponseDto {
  long  id;
  String login;
  String firstName;
  String secondName;
  String description;
  Set<String> roles;
}
