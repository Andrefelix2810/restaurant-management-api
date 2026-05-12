package com.restaurantsystem.restaurantmanagementapi.repository;

import com.restaurantsystem.restaurantmanagementapi.entity.Address;
import com.restaurantsystem.restaurantmanagementapi.entity.User;
import com.restaurantsystem.restaurantmanagementapi.enums.Role;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = new UserRowMapper();

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        }

        update(user);
        return user;
    }

    public List<User> findAll() {
        return jdbcTemplate.query("""
                SELECT id, name, email, login, password, last_modified_date, role,
                       street, number, neighborhood, city, state, zip_code, complement
                FROM users
                ORDER BY id
                """, userRowMapper);
    }

    public Optional<User> findById(Long id) {
        return queryForOptional("""
                SELECT id, name, email, login, password, last_modified_date, role,
                       street, number, neighborhood, city, state, zip_code, complement
                FROM users
                WHERE id = :id
                """, Map.of("id", id));
    }

    public boolean existsByEmail(String email) {
        return existsBy("email", email);
    }

    public boolean existsByLogin(String login) {
        return existsBy("login", login);
    }

    public Optional<User> findByEmail(String email) {
        return queryForOptional("""
                SELECT id, name, email, login, password, last_modified_date, role,
                       street, number, neighborhood, city, state, zip_code, complement
                FROM users
                WHERE email = :email
                """, Map.of("email", email));
    }

    public Optional<User> findByLogin(String login) {
        return queryForOptional("""
                SELECT id, name, email, login, password, last_modified_date, role,
                       street, number, neighborhood, city, state, zip_code, complement
                FROM users
                WHERE login = :login
                """, Map.of("login", login));
    }

    public void delete(User user) {
        jdbcTemplate.update("DELETE FROM users WHERE id = :id", Map.of("id", user.getId()));
    }

    private User insert(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update("""
                INSERT INTO users (
                    name, email, login, password, last_modified_date, role,
                    street, number, neighborhood, city, state, zip_code, complement
                )
                VALUES (
                    :name, :email, :login, :password, :lastModifiedDate, :role,
                    :street, :number, :neighborhood, :city, :state, :zipCode, :complement
                )
                """, parameters(user), keyHolder, new String[]{"id"});

        Number id = keyHolder.getKey();
        if (id != null) {
            user.setId(id.longValue());
        }

        return user;
    }

    private void update(User user) {
        jdbcTemplate.update("""
                UPDATE users
                SET name = :name,
                    email = :email,
                    login = :login,
                    password = :password,
                    last_modified_date = :lastModifiedDate,
                    role = :role,
                    street = :street,
                    number = :number,
                    neighborhood = :neighborhood,
                    city = :city,
                    state = :state,
                    zip_code = :zipCode,
                    complement = :complement
                WHERE id = :id
                """, parameters(user));
    }

    private boolean existsBy(String column, String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM users WHERE " + column + " = :value",
                Map.of("value", value),
                Integer.class
        );
        return count != null && count > 0;
    }

    private Optional<User> queryForOptional(String sql, Map<String, ?> parameters) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, parameters, userRowMapper));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private MapSqlParameterSource parameters(User user) {
        Address address = user.getAddress();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("name", user.getName())
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("password", user.getPassword())
                .addValue("lastModifiedDate", Timestamp.valueOf(user.getLastModifiedDate()))
                .addValue("role", user.getRole().name());

        if (address == null) {
            return parameters
                    .addValue("street", null)
                    .addValue("number", null)
                    .addValue("neighborhood", null)
                    .addValue("city", null)
                    .addValue("state", null)
                    .addValue("zipCode", null)
                    .addValue("complement", null);
        }

        return parameters
                .addValue("street", address.getStreet())
                .addValue("number", address.getNumber())
                .addValue("neighborhood", address.getNeighborhood())
                .addValue("city", address.getCity())
                .addValue("state", address.getState())
                .addValue("zipCode", address.getZipCode())
                .addValue("complement", address.getComplement());
    }

    private static class UserRowMapper implements RowMapper<User> {

        @Override
        public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            Address address = new Address(
                    resultSet.getString("street"),
                    resultSet.getString("number"),
                    resultSet.getString("neighborhood"),
                    resultSet.getString("city"),
                    resultSet.getString("state"),
                    resultSet.getString("zip_code"),
                    resultSet.getString("complement")
            );

            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getString("login"),
                    resultSet.getString("password"),
                    resultSet.getObject("last_modified_date", LocalDateTime.class),
                    Role.valueOf(resultSet.getString("role")),
                    address
            );
        }
    }
}
