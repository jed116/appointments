package tech.itpark.crypto;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

// hash -> Std Lib
//      -> BouncyCastle -> Australia
@RequiredArgsConstructor
public class PasswordHasherDefaultImpl implements PasswordHasher {
  private final SecureRandom random = new SecureRandom();
  public static final int DEFAULT_SALT_LENGTH = 16;
  private final MessageDigest digest;
  private final String separator = ":";

  @Override
  public String hash(String raw, int saltLength) {
    final var salt = new byte[saltLength];
    random.nextBytes(salt);
    final var saltHex = Hex.encode(salt);
    final var hash = digest.digest((saltHex + separator + raw).getBytes(StandardCharsets.UTF_8));
    final var hashHex = Hex.encode(hash);
    return saltHex + separator + hashHex;
    // TODO: hex(salt) : hex(hash(hex(salt) : raw)))
  }

  // raw, plaintext
  @Override
  public String hash(String raw) {
    return hash(raw, DEFAULT_SALT_LENGTH);
  }

  @Override
  public boolean matches(String hash, String raw) {
    final var parts = hash.split(separator);
    if (parts.length != 2) {
      throw new RuntimeException("bad password hash");
    }
    final var saltHex = parts[0];
    final var hashHex = parts[1];
    final var calculatedHashHex = Hex.encode(digest.digest(
        (saltHex + separator + raw).getBytes(StandardCharsets.UTF_8)
    ));
    return hashHex.equals(calculatedHashHex);
  }
}
