package tech.itpark.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Проверка прав доступа
// TODO: Идентификация - заявление
// TODO: Аутентификация - подтверждение секрет (что-то что трудно подделать)
// TODO: Авторизация - выдача прав доступа (ownerId = myId, moderator (PERMISSION))
@NoArgsConstructor
@AllArgsConstructor
@Data
// credentials ->
public class LoginResponseDto {
  private String token;
}
