package tech.itpark.crypto;

import java.security.SecureRandom;

public class TokenGeneratorDefaultImpl implements TokenGenerator {
  private final SecureRandom random = new SecureRandom();

  @Override
  public String generate() {
    final var bytes = new byte[128];
    random.nextBytes(bytes);
    return Hex.encode(bytes);
  }
}
