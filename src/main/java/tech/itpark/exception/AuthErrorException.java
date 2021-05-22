package tech.itpark.exception;

public class AuthErrorException extends RuntimeException {
  public AuthErrorException() {
    super();
  }

  public AuthErrorException(String message) {
    super(message);
  }

  public AuthErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthErrorException(Throwable cause) {
    super(cause);
  }

  protected AuthErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
