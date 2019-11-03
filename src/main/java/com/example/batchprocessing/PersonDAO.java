package com.example.batchprocessing;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class PersonDAO {
    private JdbcTemplate jdbcTemplate;
    private Connection connection;

    private DataSource mysqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/dbname");
        dataSource.setUsername("root");
        dataSource.setPassword("my-secret-pw");
        return dataSource;
    }

    public PersonDAO() throws SQLException {
        DataSource dataSource = mysqlDataSource();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.connection = dataSource.getConnection();
    }

    public void setupTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS people CASCADE");
        jdbcTemplate.execute("CREATE TABLE people (PersonID int, Last_Name char(50), First_Name char(50))");
    }

    public void batchUpsert() throws SQLException {
        final int numPpl = 100;
        final List<Person> people = IntStream.range(0, numPpl).boxed()
                .map(i -> new Person(i, String.format("First %d", i), String.format("Last %d", i)))
                .collect(Collectors.toList());
        final int batchSize = 10;
        final String sql = "INSERT INTO people (PersonID, Last_Name, First_Name) VALUES (?,?,?);";
        PreparedStatement ps = connection.prepareStatement(sql);
        for (int i = 0; i < numPpl/batchSize; i++) {
            System.out.println(String.format("Updating batch %d", i));
            final List<Person> sublist = people.subList(i * batchSize, (i+1) * batchSize);
            addToBatchStatement(ps, sublist);
            try {
                ps.executeBatch();
            } catch (SQLException e) {
                System.out.println("Error message: " + e.getMessage());
                return;
            }
        }
        ps.close();
    }

    private void addToBatchStatement(PreparedStatement ps, List<Person> sublist) {
        sublist.forEach(person -> {
            try {
                ps.setInt(1, person.getId());
                ps.setString(2, person.getLastName());
                ps.setString(3, person.getFirstName());
                ps.addBatch();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    public List<Person> getPeople() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT PersonID, First_Name, Last_Name FROM people ORDER BY PersonID");
        ResultSet rs = preparedStatement.executeQuery();
        List<Person> people = new ArrayList<>();
        while (rs.next()) {
            people.add(new Person(rs.getInt(1),
                    rs.getString(2).trim(),
                    rs.getString(3).trim()));
        }
        return people;
    }

    public List<Person> upsertPeople() throws SQLException {
        setupTable();
        batchUpsert();
        return getPeople();
    }
}

