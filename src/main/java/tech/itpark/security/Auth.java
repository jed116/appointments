package tech.itpark.security;

import java.util.Arrays;
import java.util.Set;

// Collection, Collections
// Auth, Auths <- utility classes: 1. static methods, 2. "const"
public interface Auth {
  // by default public static final
  String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

  long getId();
  Set<String> getRoles();

  default boolean hasRole(String role) {
    return hasAnyRole(role);
  }

  // TODO: ROLE_ADMIN (v), ROLE_USER (x) -> v -> allow wins
  // TODO: ROLE_ADMIN (v), ROLE_USER (x) -> x (Windows) -> deny wins
  default boolean hasAnyRole(String ...roles) {
    return Arrays.stream(roles).anyMatch(o -> getRoles().contains(o));
  }

  static Auth anonymous() {
    return new Auth() {
      @Override
      public long getId() {
        return -1;
      }

      @Override
      public Set<String> getRoles() {
        return Set.of(ROLE_ANONYMOUS);
      }
    };
  }
}
