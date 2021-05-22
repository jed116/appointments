package tech.itpark.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface Middleware {
  boolean process(HttpServletRequest request, HttpServletResponse response);
}

// TODO:
//   1. true - значит дальше запрос обрабатывать не нужно
//     exception - дальше запрос обрабатывать не нужно
//   2. Chain of Responsibility