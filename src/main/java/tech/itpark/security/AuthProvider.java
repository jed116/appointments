package tech.itpark.security;

@FunctionalInterface
public interface AuthProvider {
  Auth provide(String token);
}
