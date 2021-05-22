package tech.itpark.dto;

import lombok.Value;

@Value
public class UpdateSecretRequestDto {
    private String login;
    private String password;
    private String secret;
}
