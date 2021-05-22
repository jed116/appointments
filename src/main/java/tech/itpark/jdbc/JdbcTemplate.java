package tech.itpark.jdbc;

import tech.itpark.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTemplate {
  public int update(Connection conn, String sql, Object... params) {
    try (
        final var stmt = conn.prepareStatement(sql);
    ) {
      var index = 0;
      for (Object param : params) {
        stmt.setObject(++index, param);
      }
      return stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }
}
