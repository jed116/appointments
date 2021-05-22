package tech.itpark.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tech.itpark.dto.UsersResponseDto;
import tech.itpark.exception.DataAccessException;
import tech.itpark.jdbc.JdbcTemplate;
import tech.itpark.model.TokenAuth;
import tech.itpark.model.User;
import tech.itpark.security.Roles;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepository {
  // вместо Connection работает с DataSource, который может выдавать Connection по запросу
  private final DataSource ds;
  private final JdbcTemplate template = new JdbcTemplate();


  public User save(User user) {
    try (
        final var connection = ds.getConnection();
        final var statement = connection.prepareStatement("""
                INSERT INTO users(login, password, secret) VALUES (?, ?, ?);
            """, Statement.RETURN_GENERATED_KEYS); // альтернатива RETURNING
    ) {
      var index = 0;
      statement.setString(++index, user.getLogin());
      statement.setString(++index, user.getPassword());
      statement.setString(++index, user.getSecret());
      statement.executeUpdate();

      final var keys = statement.getGeneratedKeys();
      if (!keys.next()) {
        throw new DataAccessException("no keys in result");
      }

      user.setId(keys.getLong(1));

      return user;
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public void remove(User user, Boolean removed) {
    try (
            final var statement = ds.getConnection().prepareStatement("UPDATE users SET removed = ? WHERE id = ?")
    ) {
      var index = 0;
      statement.setBoolean(++index,  removed);
      statement.setLong(++index,  user.getId());
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  private Optional<User> get(Optional<String> login, Optional<String> token) {
    try (
            final var connection = ds.getConnection();
            final var statement = connection.prepareStatement("""
        SELECT u.id AS id, u.login AS login, u.password AS password, u.secret AS secret, u.removed AS removed,
          COALESCE(ui.firstname, '') AS firstName, COALESCE(ui.secondname, '') AS secondName, COALESCE(ui.description, '') AS description,
          ARRAY_AGG( COALESCE(r.name, ?)) AS roles FROM  users u
        LEFT JOIN user_roles u_r ON (u.id = u_r.user_id AND u_r.active)
        LEFT JOIN roles r on r.id = u_r.role_id
        LEFT JOIN user_info ui on u.id = ui.user_id
        LEFT JOIN tokens t ON u.id = t.userid
          WHERE (?  OR u.login = ?) AND (? OR t.token = ?)
        GROUP BY u.id, ui.firstname, ui.secondname, ui.description
          """);
    ) {
      var index = 0;
      statement.setString(++index, Roles.ROLE_ANONYMOUS);
      statement.setBoolean(++index, login.isEmpty());
      statement.setString(++index, !login.isEmpty() ? login.get() : "");
      statement.setBoolean(++index, token.isEmpty());
      statement.setString(++index, !token.isEmpty() ? token.get() : "");
      try (
              final var resultSet = statement.executeQuery();
      ) {
        return resultSet.next() ? Optional.of(
                new User(
                        resultSet.getLong("id"),
                        resultSet.getString("login"),
                        resultSet.getString("password"),
                        resultSet.getString("secret"),
                        resultSet.getBoolean("removed"),
                        Set.of((String[])resultSet.getArray("roles").getArray()),
                        resultSet.getString("firstName"),
                        resultSet.getString("secondName"),
                        resultSet.getString("description")                )
        ) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }

  }

  public Optional<User> getByLogin(String login) {
    return login != null ? get(Optional.of(login) , Optional.empty()) : Optional.empty();
  }

  public Optional<User> getByToken(String token) {
    return token != null ? get(Optional.empty(), Optional.of(token)) : Optional.empty();
  }


  public void saveRoles(long id, Set<String> roles){
    if (id == 0 || roles.size() == 0){
      return;
    }
    try (
      final var connection = ds.getConnection();
      final var statement = connection.prepareStatement("""
          INSERT INTO user_roles(user_id, role_id, active)
          SELECT ?, r.id, FALSE FROM roles r WHERE r.name = ANY (?)
            """, Statement.NO_GENERATED_KEYS);
    ) {

      String[] arrayString = roles.toArray(String[]::new);
      Array arrayRoles = connection.createArrayOf("TEXT", arrayString);

      var index = 0;
      statement.setLong(++index, id);
      statement.setArray(++index,  arrayRoles);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public void updateRoles(long id, Set<String> roles, boolean active){
    if (id == 0 || roles.size() == 0){
      return;
    }
    try (
      final var connection = ds.getConnection();
      final var statement = connection.prepareStatement("""
        UPDATE user_roles SET active = ? WHERE user_id = ? AND
         role_id IN (SELECT r.id FROM roles r WHERE r.name = ANY (?))
            """, Statement.NO_GENERATED_KEYS);
    ){
      String[] arrayString = roles.toArray(String[]::new);
      Array arrayRoles = connection.createArrayOf("TEXT", arrayString);

      var index = 0;
      statement.setBoolean(++index, active);
      statement.setLong(++index, id);
      statement.setArray(++index,  arrayRoles);
      statement.executeUpdate();

    }catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }


  public void updatePassword(User user) {
    try (
      final var statement = ds.getConnection().prepareStatement("UPDATE users SET password = ? WHERE id = ?")
    ) {
      var index = 0;
      statement.setString(++index,user.getPassword());
      statement.setLong(++index,  user.getId());
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public void updateSecret(User user) {
    try (
      final var statement = ds.getConnection().prepareStatement("UPDATE users SET secret = ? WHERE id = ?")
    ) {
      var index = 0;
      statement.setString(++index,user.getSecret());
      statement.setLong(++index,  user.getId());
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }





  public void saveToken(TokenAuth auth) {
    try (
            final var conn = ds.getConnection();
    ) {
      // language=PostgreSQL
      template.update(conn,"INSERT INTO tokens(userId, token) VALUES (?, ?) ON CONFLICT (userId) DO UPDATE SET token = ?",
              auth.getUserId(), auth.getToken(), auth.getToken());
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public void deleteToken(TokenAuth auth) {
    try(
      final var conn = ds.getConnection();
    ) {
      // language=PostgreSQL
      template.update(conn, "DELETE FROM tokens WHERE userId = ? AND token = ?;",
              auth.getUserId(), auth.getToken());
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }


  public List<User> getUserRoles(Set<String> roles, int active) {
    List<User> result = new ArrayList<>();
    try (
      final var connection = ds.getConnection();
      final var statement = connection.prepareStatement("""
        SELECT u.id AS id, u.login AS login, '********' AS password, '********' AS secret, u.removed AS removed,
         array_agg(r.name) AS roles FROM  users u
        LEFT JOIN user_roles u_r ON u.id = u_r.user_id
        LEFT JOIN roles r on r.id = u_r.role_id
        WHERE u_r.role_id IN (SELECT r.id FROM roles r WHERE r.name = ANY (?)) AND (u_r.active = ? OR ?)  
        GROUP BY u.id
        """, Statement.NO_GENERATED_KEYS);
    ){
      String[] arrayString = roles.toArray(String[]::new);
      Array arrayRoles = connection.createArrayOf("TEXT", arrayString);

      var index = 0;
      statement.setArray(++index,  arrayRoles);
      statement.setBoolean(++index, active > 0);
      statement.setBoolean(++index, active == 0);

      try (
         final var resultSet = statement.executeQuery();
      ) {
        while (resultSet.next()) {
          result.add(new User(resultSet.getLong("id"),
                              resultSet.getString("login"),
                              resultSet.getString("password"),
                              resultSet.getString("secret"),
                              resultSet.getBoolean("removed"),
                              Set.of((String[])resultSet.getArray("roles").getArray()),
                             "","","")
                    );
        }
      }
    }catch (SQLException e) {
      throw new DataAccessException(e);
    }
    return result;
  }

  public void activeUserRoles(long id, Set<String> roles, boolean active) {
    try (
      final var connection = ds.getConnection();
      final var statement = connection.prepareStatement("""
        UPDATE user_roles SET active = ? WHERE user_id = ? AND role_id IN (SELECT r.id FROM roles r WHERE r.name = ANY(?)) 
        """);
    ) {
      String[] arrayString = roles.toArray(String[]::new);
      Array arrayRoles = connection.createArrayOf("TEXT", arrayString);

      var index = 0;
      statement.setBoolean(++index, active);
      statement.setLong(++index, id);
      statement.setArray(++index,  arrayRoles);

      statement.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }

  }

  public void setUserInfo(long id, String firstName, String secondName, String description) {
    try (
        final var conn = ds.getConnection();
    ) {
      // language=PostgreSQL
      template.update(conn,"""
                      INSERT INTO user_info(user_id, firstName, secondName, description)
                      VALUES (?, ?, ?, ?) ON CONFLICT (user_id) DO UPDATE SET firstName = ?, secondName = ?, description = ?
                      """, id, firstName, secondName, description,            firstName,     secondName,     description);
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }

  }

  public List<User> findUsers(Set<String> rolesFilter, Set<String> infoFilter) {
    List<User> result = new ArrayList<>();
    try (
      final var connection = ds.getConnection();
      final var statement = connection.prepareStatement("""
        SELECT u.id AS id, u.login AS login, '********' AS password, '********' AS secret,
        COALESCE(ui.firstname, '') AS firstName, COALESCE(ui.secondname, '') AS secondName, COALESCE(ui.description, '') AS description,
        ARRAY_AGG( COALESCE(r.name, ?)) AS roles
        FROM  users u
          LEFT JOIN user_roles u_r ON u.id = u_r.user_id AND u_r.active
          LEFT JOIN roles r on r.id = u_r.role_id
          LEFT JOIN user_info ui on u.id = ui.user_id
        WHERE NOT u.removed AND
              (?  OR u_r.role_id IN (SELECT rr.id FROM roles rr WHERE rr.name = ANY (?))) AND
              (?  OR LOWER(ui.firstName) LIKE ANY (?) OR LOWER(ui.secondname) LIKE ANY (?) OR LOWER(ui.description) LIKE ANY (?))
        GROUP BY u.id, ui.firstname, ui.secondname, ui.description
        """, Statement.NO_GENERATED_KEYS);
    ){
      String[] rolesFilterStringArray = rolesFilter.toArray(String[]::new);
      Array rolesConnectionArray = connection.createArrayOf("TEXT", rolesFilterStringArray);

      String[] infoFilterStringArray = infoFilter.stream().map(o -> "%" + o.toLowerCase() + "%").toArray(String[]::new);
      Array infoConnectionArray = connection.createArrayOf("TEXT", infoFilterStringArray);

      var index = 0;
      statement.setString(++index,  Roles.ROLE_ANONYMOUS);
      statement.setBoolean(++index, rolesFilterStringArray.length == 0);
      statement.setArray(++index,   rolesConnectionArray);
      statement.setBoolean(++index, infoFilterStringArray.length == 0);
      statement.setArray(++index,   infoConnectionArray);
      statement.setArray(++index,   infoConnectionArray);
      statement.setArray(++index,   infoConnectionArray);

      try (
        final var resultSet = statement.executeQuery();
      ) {
        while (resultSet.next()) {
          result.add(new User(
                  resultSet.getLong("id"),
                  resultSet.getString("login"),
                  resultSet.getString("password"),
                  resultSet.getString("secret"),
                  false,
                  Set.of((String[])resultSet.getArray("roles").getArray()),
                  resultSet.getString("firstName"),
                  resultSet.getString("secondName"),
                  resultSet.getString("description")
                              )
                    );
        }
      }
    }catch (SQLException e) {
      throw new DataAccessException(e);
    }
    return result;
  }

}
