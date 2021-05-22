package tech.itpark.dto;

import lombok.Value;

@Value
public class UpdatePasswordRequestDto {
    private String login;
    private String password;
    private String secret;
}
