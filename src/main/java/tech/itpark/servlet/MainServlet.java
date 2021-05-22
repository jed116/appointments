package tech.itpark.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tech.itpark.exception.PermissionDeniedException;
import tech.itpark.http.Handler;
import tech.itpark.middleware.Middleware;
import tech.itpark.security.Auth;

import java.io.IOException;
import java.util.*;

// Servlet
public class MainServlet extends HttpServlet {
  private ConfigurableApplicationContext context; // DI context
  private Map<String, Handler> routesPost;

  private Map<String, Set<String>> queriesPermissions;

//  private List<Middleware> middlewares;
  private final Handler notFoundHandler = (request, response) -> response.sendError(404, "Page not found");

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
//      middlewares = (List<Middleware>) context.getBean("middlewares");
      queriesPermissions = (Map<String, Set<String>>) context.getBean("queriesPermissions");

      // TODO: 1. Annotation Config -> @Component <- your class
      // TODO: 2. Java Config -> @Configuration @Bean <- not your class, initialization logic
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

//    for (final var middleware : middlewares) {
//      if (middleware.process(request, response)) {
//        return;
//      }
//    }

    final var servletPath = request.getServletPath();

//    if (queriesPermissions.containsKey(servletPath)){
//      final var auth = (Auth) request.getAttribute("AUTH");
//      final var roles = queriesPermissions.get(servletPath).toArray(String[]::new);
//      if (!auth.hasAnyRole(roles)){
//        int p = 0;
//        throw new PermissionDeniedException("NOT ALLOWED!!!");
//      }
//    }

    try {
      Optional.ofNullable(routesPost.get(servletPath))
          .orElse(notFoundHandler)
          .handle(request, response);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
