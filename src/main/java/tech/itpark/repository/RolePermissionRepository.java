package tech.itpark.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tech.itpark.configuration.AppParams;
import tech.itpark.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RolePermissionRepository {
    private final DataSource dataSource;


    public Set<String> getOperationRoles(String operation) {
        try (
                final var connection = dataSource.getConnection();
                final var statement = connection.prepareStatement("""
        SELECT p.operation operation , ARRAY_AGG( COALESCE(r.name, ?)) AS roles FROM permissions p
        LEFT JOIN roles r on p.role_id = r.id
        WHERE LOWER(operation) = ?
        GROUP BY p.operation
        """, Statement.NO_GENERATED_KEYS);
        ){

            var index = 0;
            statement.setString(++index,  AppParams.roleAnonymous());
            statement.setString(++index,  operation);

            try (
                    final var resultSet = statement.executeQuery();
            ) {
                while (resultSet.next()) {
                    return Set.of((String[])resultSet.getArray("roles").getArray());
                }
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return Set.of();
    }

    public Map<String, Set<String>> findPermissions(Set<String> operations, Set<String> roles) {
        Map<String, Set<String>> result = new HashMap<>();
        try (
                final var connection = dataSource.getConnection();
                final var statement = connection.prepareStatement("""
        SELECT p.operation operation , ARRAY_AGG( COALESCE(r.name, ?)) AS roles FROM permissions p
        LEFT JOIN roles r on p.role_id = r.id
        WHERE (?  OR LOWER(operation) LIKE ANY (?)) AND (?  OR r.name LIKE ANY (?))
        GROUP BY p.operation
        """, Statement.NO_GENERATED_KEYS);
        ){
            Array operationsArray = connection.createArrayOf("TEXT", operations.stream().map(o -> "%" + o.toLowerCase() + "%").toArray(String[]::new));
            Array rolesArray = connection.createArrayOf("TEXT", roles.toArray(String[]::new));

            var index = 0;
            statement.setString(++index,  AppParams.roleAnonymous());
            statement.setBoolean(++index, operations.size() == 0);
            statement.setArray(++index,   operationsArray);
            statement.setBoolean(++index, roles.size() == 0);
            statement.setArray(++index,   rolesArray);

            try (
                    final var resultSet = statement.executeQuery();
            ) {
                while (resultSet.next()) {
                    result.put(resultSet.getString("operation"), Set.of((String[])resultSet.getArray("roles").getArray()));
                }
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return result;
    }

    public void appendPermissions(String operation, Set<String> roles) {
        try (
                final var connection = dataSource.getConnection();
                final var statement = connection.prepareStatement("""
          INSERT INTO permissions(operation, role_id) 
          SELECT ?, r.id FROM roles r WHERE r.name = ANY(?)
            """,Statement.NO_GENERATED_KEYS
                );
        ) {

            Array rolesArray = connection.createArrayOf("TEXT", roles.toArray(String[]::new));

            var index = 0;
            statement.setString(++index, operation);
            statement.setArray(++index, rolesArray);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void removePermissions(String operation, Set<String> roles) {
        try(
                final var connection = dataSource.getConnection();
                final var statement = connection.prepareStatement("""
         DELETE FROM permissions WHERE operation = ? AND role_id IN (SELECT r.id FROM roles r WHERE r.name = ANY(?) )
            """, Statement.NO_GENERATED_KEYS );
        ) {
            Array rolesArray = connection.createArrayOf("TEXT", roles.toArray(String[]::new));

            var index = 0;
            statement.setString(++index, operation);
            statement.setArray(++index, rolesArray);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }


    public Map<String, Map<String, Boolean>> findRoles(Map<String, Boolean> attributes) {
        Map<String, Map<String, Boolean>> result = new HashMap<>();
        try (
                final var connection = dataSource.getConnection();
                final var statement = connection.prepareStatement("SELECT *  FROM roles", Statement.NO_GENERATED_KEYS);
        ){
            try (
                final var resultSet = statement.executeQuery();
            ) {
                while (resultSet.next()) {
                   result.put(resultSet.getString(AppParams.rolePropertyNAME()),
                            Map.of(
                                    AppParams.rolePropertyACTIVE(),     resultSet.getBoolean(AppParams.rolePropertyACTIVE()),
                                    AppParams.roleAttributeADMIN(),     resultSet.getBoolean(AppParams.roleAttributeADMIN()),
                                    AppParams.roleAttributeCHIEF(),     resultSet.getBoolean(AppParams.roleAttributeCHIEF()),
                                    AppParams.roleAttributeDOCTOR(),    resultSet.getBoolean(AppParams.roleAttributeDOCTOR()),
                                    AppParams.roleAttributePATIENT(),   resultSet.getBoolean(AppParams.roleAttributePATIENT())
                                   )
                            );
                }

                return result.entrySet().stream().filter(r -> r.getValue().entrySet().stream().allMatch(a -> attributes.containsKey(a.getKey()) ? attributes.get(a.getKey()) == a.getValue() : true)).collect(Collectors.toMap(r -> r.getKey(), r -> r.getValue()));
//                return result.entrySet().stream().filter(r -> r.getValue().entrySet().stream().anyMatch(a -> attributes.containsKey(a.getKey()) ? attributes.get(a.getKey()) == a.getValue() : false)).collect(Collectors.toMap(r -> r.getKey(), r -> r.getValue()));

            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public long appendRole(Map<String, ?> attributes) {
        try (
            final var connection = dataSource.getConnection();
            final var statement = connection.prepareStatement("INSERT INTO roles(" +
                    AppParams.rolePropertyNAME()    +   ", "+
                    AppParams.rolePropertyACTIVE()  +   ", " +
                    AppParams.roleAttributeADMIN()  +   ", " +
                    AppParams.roleAttributeCHIEF()  +   ", " +
                    AppParams.roleAttributeDOCTOR() +   ", " +
                    AppParams.roleAttributePATIENT()+   ") VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ) {
            var index = 0;
            statement.setString(++index,  (String) attributes.get(AppParams.rolePropertyNAME()) );
            statement.setBoolean(++index, (Boolean) attributes.get(AppParams.rolePropertyACTIVE()) );
            statement.setBoolean(++index, (Boolean) attributes.get(AppParams.roleAttributeADMIN()) );
            statement.setBoolean(++index, (Boolean) attributes.get(AppParams.roleAttributeCHIEF()) );
            statement.setBoolean(++index, (Boolean) attributes.get(AppParams.roleAttributeDOCTOR()) );
            statement.setBoolean(++index, (Boolean) attributes.get(AppParams.roleAttributePATIENT()) );
            statement.executeUpdate();
            final var keys = statement.getGeneratedKeys();
            if (!keys.next()) {
                throw new DataAccessException("no keys in result");
            }
            return keys.getLong(1);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }


    public long roleId(String role) {
        try (
                final var connection = dataSource.getConnection();
                final var statement = connection.prepareStatement("""
                    SELECT id FROM roles WHERE name = ? 
                    """, Statement.NO_GENERATED_KEYS);
        ){
            var index = 0;
            statement.setString(++index,  role);
            try (
                    final var resultSet = statement.executeQuery();
            ) {
                return resultSet.next() ? resultSet.getLong("id") : 0L;
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void removeRole(long id) {
        try(
            final var connection = dataSource.getConnection();
            final var statement = connection.prepareStatement("""
                    DELETE FROM roles WHERE id = ?
            """, Statement.NO_GENERATED_KEYS );
        ) {

            var index = 0;
            statement.setLong(++index, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////  I N I T

    public Map<String, Map<String, Boolean>> initRoleAttributes() {
        Map<String, Map<String, Boolean>> result = new HashMap<>();
        try (
                final var connection = dataSource.getConnection();
                final var statement = connection.prepareStatement("SELECT *  FROM roles", Statement.NO_GENERATED_KEYS);
        ){
            try (
                    final var resultSet = statement.executeQuery();
            ) {
                while (resultSet.next()) {
                    result.put(
                            resultSet.getString(AppParams.rolePropertyNAME()),
                                    Map.of( AppParams.rolePropertyACTIVE(),  resultSet.getBoolean(AppParams.rolePropertyACTIVE()),
                                            AppParams.roleAttributeADMIN(),   resultSet.getBoolean(AppParams.roleAttributeADMIN()),
                                            AppParams.roleAttributeCHIEF(),   resultSet.getBoolean(AppParams.roleAttributeCHIEF()),
                                            AppParams.roleAttributeDOCTOR(),   resultSet.getBoolean(AppParams.roleAttributeDOCTOR()),
                                            AppParams.roleAttributePATIENT(),   resultSet.getBoolean(AppParams.roleAttributePATIENT())
                                           )
                                );
                }
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return result;
    }

    public Map<String, Set<String>> initRolePermissions() {
        Map<String, Set<String>> result = new HashMap<>();
        try (
                final var connection = dataSource.getConnection();
                final var statement = connection.prepareStatement("""
        SELECT p.operation AS operation, ARRAY_AGG( COALESCE(r.name, ?)) AS roles FROM permissions p
        LEFT JOIN roles r on r.id = p.role_id
        GROUP BY p.operation        
        """, Statement.NO_GENERATED_KEYS);
        ){

            var index = 0;
            statement.setString(++index, AppParams.roleAnonymous());

            try (
                    final var resultSet = statement.executeQuery();
            ) {
                while (resultSet.next()) {
                    result.put(
                            resultSet.getString("operation"),
                            Set.of((String[])resultSet.getArray("roles").getArray())
                    );
                }
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return result;
    }

}
