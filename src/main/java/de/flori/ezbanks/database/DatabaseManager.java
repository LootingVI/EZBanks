package de.flori.ezbanks.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {

    private HikariDataSource dataSource;

    public DatabaseManager(String host, int port, String username, String password, String database, String poolName) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return;
        }

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName(poolName);

        config.setMaximumPoolSize(8);
        config.setMinimumIdle(10);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);

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

        this.dataSource = new HikariDataSource(config);
    }

    @SuppressWarnings("ALL")
    public boolean isConnected() {
        return dataSource != null;
    }

    public void disconnect() {
        if (!isConnected()) return;
        dataSource.close();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public ResultSet getResult(String query) {
        if (!isConnected()) throw new RuntimeException("datasource is not connected!");

        try (Connection connection = getConnection()) {
            final PreparedStatement statement = connection.prepareStatement(query);
            return statement.executeQuery();
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public int update(String query) {
        if (!isConnected()) throw new RuntimeException("datasource is not connected!");

        try (Connection connection = getConnection()) {
            final PreparedStatement statement = connection.prepareStatement(query);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return -1;
        }
    }

}
