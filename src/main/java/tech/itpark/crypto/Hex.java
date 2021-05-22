package tech.itpark.crypto;

import java.nio.charset.StandardCharsets;

public class Hex {
  private Hex() {
  }

  private static final char[] chars = "0123456789abcdef".toCharArray();

  // Apache Commons -> Libraries
  // Google Guava
  // Spring Libraries
  public static String encode(byte[] bytes) {
    final var result = new char[2 * bytes.length];
    // 11110000 -> 1 + 2 + 4 + 8 -> 0-15
    // 1 * 2^0 + 1 * 2^1 + 1 * 2^2 + 1 * 2^3 -> 15
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      result[i * 2] = chars[(0b1111_0000 & b) >>> 4]; // 0xF0
      result[i * 2 + 1] = chars[0b0000_1111 & b];
    }
    return new String(result);
  }
}
