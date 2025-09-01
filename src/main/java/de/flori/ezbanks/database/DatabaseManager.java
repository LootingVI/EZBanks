package de.flori.ezbanks.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.flori.ezbanks.manager.impl.DatabaseType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private HikariDataSource dataSource;
    private final DatabaseType type;
    private volatile boolean closed = false;

    public DatabaseManager(DatabaseType type, String host, int port, String username, String password, String database, String poolName) {
        this.type = type;

        try {
            if (type == DatabaseType.MYSQL) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if (type == DatabaseType.SQLITE) {
                Class.forName("org.sqlite.JDBC");
            } else {
                throw new IllegalArgumentException("Unsupported database type: " + type);
            }
        } catch (ClassNotFoundException exception) {
            LOGGER.log(Level.SEVERE, "Database driver not found for type: " + type, exception);
            throw new RuntimeException("Database driver not found!", exception);
        }

        HikariConfig config = new HikariConfig();

        try {
            if (type == DatabaseType.MYSQL) {
                configureMySQLConnection(config, host, port, username, password, database, poolName);
            } else {
                configureSQLiteConnection(config, database);
            }

            this.dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database connection", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    private void configureMySQLConnection(HikariConfig config, String host, int port, String username, String password, String database, String poolName) {
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName(poolName);
        config.setMaximumPoolSize(10); // Increased from 8
        config.setMinimumIdle(5);      // Fixed: was higher than max pool size
        config.setMaxLifetime(1800000L);
        config.setConnectionTimeout(30000L); // Increased timeout
        config.setIdleTimeout(600000L);      // Added idle timeout
        config.setLeakDetectionThreshold(60000L); // Added leak detection

        // MySQL specific optimizations
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
    }

    private void configureSQLiteConnection(HikariConfig config, String database) {
        config.setJdbcUrl("jdbc:sqlite:" + database);
        config.setMaximumPoolSize(1); // SQLite should use single connection
        config.setConnectionTimeout(30000L);
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
    }

    public DatabaseType getType() {
        return this.type;
    }

    public boolean isConnected() {
        return this.dataSource != null && !this.dataSource.isClosed() && !this.closed;
    }

    public synchronized void disconnect() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            try {
                this.dataSource.close();
                this.closed = true;
                LOGGER.info("Database connection closed successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while closing database connection", e);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Database is not connected or has been closed");
        }
        return this.dataSource.getConnection();
    }

    @Deprecated
    public ResultSet getResult(String query) {
        throw new UnsupportedOperationException("Use executeQuery with proper resource management instead");
    }

    // Bessere Alternative für SELECT queries
    public <T> T executeQuery(String query, ResultSetProcessor<T> processor) throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Database is not connected or has been closed");
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            return processor.process(resultSet);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query: " + query, e);
            throw e;
        }
    }

    // Überladene Methode mit Parametern
    public <T> T executeQuery(String query, Object[] parameters, ResultSetProcessor<T> processor) throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Database is not connected or has been closed");
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Parameter setzen
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return processor.process(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing parameterized query: " + query, e);
            throw e;
        }
    }

    public int executeUpdate(String query) throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Database is not connected or has been closed");
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            return statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing update: " + query, e);
            throw e;
        }
    }

    // Überladene Methode mit Parametern
    public int executeUpdate(String query, Object[] parameters) throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Database is not connected or has been closed");
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Parameter setzen
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing parameterized update: " + query, e);
            throw e;
        }
    }

    // Functional Interface für ResultSet Processing
    @FunctionalInterface
    public interface ResultSetProcessor<T> {
        T process(ResultSet resultSet) throws SQLException;
    }
}