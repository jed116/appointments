package tech.itpark.security;

import jakarta.servlet.http.HttpServletRequest;

public class HttpServletRequestAuthToken {
  private HttpServletRequestAuthToken() {}

  public static Auth auth(HttpServletRequest request) {
    return (Auth) request.getAttribute("AUTH");
  }

  public static String token(HttpServletRequest request){
    return (String) request.getAttribute("TOKEN");
  }

}
