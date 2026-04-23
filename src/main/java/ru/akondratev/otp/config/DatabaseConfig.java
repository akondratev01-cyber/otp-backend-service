package ru.akondratev.otp.config;

import org.postgresql.ds.PGSimpleDataSource;
import javax.sql.DataSource;

public class DatabaseConfig {

    private final ApplicationProperties properties;

    public DatabaseConfig(ApplicationProperties properties) {
        this.properties = properties;
    }

    public DataSource dataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(properties.get("db.url"));
        dataSource.setUser(properties.get("db.username"));
        dataSource.setPassword(properties.get("db.password"));
        return dataSource;
    }
}
