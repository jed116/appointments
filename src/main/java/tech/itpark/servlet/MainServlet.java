package tech.itpark.servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tech.itpark.exception.PermissionDeniedException;
import tech.itpark.http.ContentTypes;
import tech.itpark.http.Handler;

import java.io.IOException;
import java.util.*;

// Servlet
public class MainServlet extends HttpServlet {
  private ConfigurableApplicationContext context; // DI context
  private Map<String, Handler> routesPost;

  private final Handler notFoundHandler = (request, response) -> sendError(404, "NOT FOUND !!!", response);

  // init
  // destroy
  // service
  @Override
  public void init() throws ServletException {
    super.init();
    try {
      final var basePackage = getInitParameter("base-package");
      context = new AnnotationConfigApplicationContext(basePackage);

      final var servletContext = getServletContext();
      servletContext.setAttribute("CONTEXT", context);

      routesPost = (Map<String, Handler>) context.getBean("routesPost");

      Map<String, Set<String>> rolePermissions = (Map<String, Set<String>>) context.getBean("rolePermissions");
      servletContext.setAttribute("PERMISSIONS", rolePermissions);

    } catch (Exception e) {
      throw new UnavailableException(e.getMessage());
    }
  }

  @Override
  public void destroy() {
    context.close();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//    super.doPost(req, resp);
    try {
      Optional.ofNullable(routesPost.get(request.getServletPath()))
          .orElse(notFoundHandler)
          .handle(request, response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendError(int StatusCode, String message, HttpServletResponse response) throws IOException {
    response.setStatus(StatusCode);
    response.setContentType(ContentTypes.APPLICATION_JSON);
    response.getWriter().write((new Gson()).toJson( Map.of("message", message)));
  }
}
