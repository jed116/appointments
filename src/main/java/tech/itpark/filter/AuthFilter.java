package tech.itpark.filter;

import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import tech.itpark.exception.PermissionDeniedException;
import tech.itpark.http.ContentTypes;
import tech.itpark.security.Auth;
import tech.itpark.security.AuthProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class AuthFilter extends HttpFilter {
//  private AuthProvider provider;
//  @Override
//  public void init() throws ServletException {
//    final var context = (ApplicationContext) getServletContext().getAttribute("CONTEXT");
//    provider = context.getBean(AuthProvider.class);
//  }
  private void sendError(int StatusCode, String message, HttpServletResponse response) throws IOException {
    response.setStatus(StatusCode);
    response.setContentType(ContentTypes.APPLICATION_JSON);
    response.getWriter().write((new Gson()).toJson( Map.of("message", message)));
  }

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    final var token = req.getHeader("Authorization");
    req.setAttribute("TOKEN", token);

    final var context = (ApplicationContext) getServletContext().getAttribute("CONTEXT");
    AuthProvider provider = context.getBean(AuthProvider.class);

    final var auth = provider.provide(token);
    req.setAttribute("AUTH", auth);

    final var servletPath = req.getServletPath();
    final var rolePermissions = (Map<String, Set<String>>) getServletContext().getAttribute("PERMISSIONS");

    try{
        if (rolePermissions.containsKey(servletPath)) {
          final var roles = rolePermissions.get(servletPath).toArray(String[]::new);
          if (!auth.hasAnyRole(roles)) {
            throw new PermissionDeniedException("FORBIDDEN!!! Not allowed operation.");
          }
        }
    }catch (PermissionDeniedException e){
      e.printStackTrace();
      sendError(403, e.getMessage(), res);
      return;
    }


    chain.doFilter(req, res);
  }
}
