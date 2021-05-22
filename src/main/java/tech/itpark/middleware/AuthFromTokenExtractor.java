package tech.itpark.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tech.itpark.security.AuthProvider;

@RequiredArgsConstructor
public class AuthFromTokenExtractor implements Middleware {
  private final AuthProvider provider;

  @Override
  public boolean process(HttpServletRequest request, HttpServletResponse response) {
    final var token = (String) request.getAttribute("TOKEN");
    final var auth = provider.provide(token);
    request.setAttribute("AUTH", auth);
    return false;
  }
}
