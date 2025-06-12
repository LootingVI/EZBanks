package de.flori.ezbanks.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.flori.ezbanks.manager.impl.DatabaseType;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final DatabaseType type;

    public DatabaseManager(DatabaseType type, String host, int port, String username, String password, String database, String poolName) {
        this.type = type;

        try {
            if (type == DatabaseType.MYSQL) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else {
                Class.forName("org.sqlite.JDBC");
            }
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            throw new RuntimeException("Database driver not found!");
        }

        HikariConfig config = new HikariConfig();
        if (type == DatabaseType.MYSQL) {
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setPoolName(poolName);
            config.setMaximumPoolSize(8);
            config.setMinimumIdle(10);
            config.setMaxLifetime(1800000L);
            config.setConnectionTimeout(5000L);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            config.addDataSourceProperty("alwaysSendSetIsolation", "false");
            config.addDataSourceProperty("cacheCallableStmts", "true");
        } else {
            config.setJdbcUrl("jdbc:sqlite:" + database);
        }

        this.dataSource = new HikariDataSource(config);
    }

    public DatabaseType getType() {
        return this.type;
    }

    public boolean isConnected() {
        return this.dataSource != null;
    }

    public void disconnect() {
        if (this.isConnected()) {
            this.dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public ResultSet getResult(String query) {
        if (!this.isConnected()) {
            throw new RuntimeException("datasource is not connected!");
        } else {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(query);
                return statement.executeQuery();
            } catch (SQLException exception) {
                exception.printStackTrace(new PrintStream(System.err));
                return null;
            }
        }
    }

    public int update(String query) {
        if (!this.isConnected()) {
            throw new RuntimeException("datasource is not connected!");
        } else {
            try (Connection connection = this.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(query);
                return statement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace(new PrintStream(System.err));
                return -1;
            }
        }
    }
}
