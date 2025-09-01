package de.flori.ezbanks.config;

import de.flori.ezbanks.EZBanks;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    // Default Werte
    private static final String DEFAULT_PREFIX = "§cEZBank §7>> ";
    private static final String DEFAULT_CURRENCY_SYMBOL = "$";
    private static final int DEFAULT_CARD_COST = 1000;
    private static final int DEFAULT_BANK_ACCOUNT_COST = 0;
    private static final String DEFAULT_DB_FILE = "plugins/EZBanks/database.db";
    private static final int DEFAULT_DB_PORT = 3306;

    // MySQL Placeholder-Werte
    private static final List<String> MYSQL_PLACEHOLDERS = Arrays.asList(
            "Type ur MYSQL password here",
            "Type ur MYSQL username here",
            "Type ur MYSQL ip here",
            "Type ur MYSQL database name here",
            "localhost",
            "username",
            "password",
            "database"
    );

    private final EZBanks plugin;

    public ConfigManager() {
        this.plugin = EZBanks.getInstance();
        if (this.plugin == null) {
            throw new IllegalStateException("ConfigManager cannot be initialized before plugin instance is available");
        }
    }

    public void createConfig() {
        try {
            LOGGER.info("Creating default configuration...");

            FileConfiguration config = plugin.getConfig();

            // Database Konfiguration
            setDefaultValue(config, "database.type", "sqlite");
            setDefaultValue(config, "database.host", "localhost");
            setDefaultValue(config, "database.port", DEFAULT_DB_PORT);
            setDefaultValue(config, "database.username", "root");
            setDefaultValue(config, "database.password", "password");
            setDefaultValue(config, "database.database", "ezbanks");
            setDefaultValue(config, "database.file", DEFAULT_DB_FILE);

            // Plugin Konfiguration
            setDefaultValue(config, "prefix", DEFAULT_PREFIX);
            setDefaultValue(config, "currency_symbol", DEFAULT_CURRENCY_SYMBOL);
            setDefaultValue(config, "new_card_cost", DEFAULT_CARD_COST);
            setDefaultValue(config, "bank_account_cost", DEFAULT_BANK_ACCOUNT_COST);

            // Feature Flags
            setDefaultValue(config, "send_anonymous_data", true);
            setDefaultValue(config, "bedrock_support", false);
            setDefaultValue(config, "auto_update", false); // Standardmäßig deaktiviert für Sicherheit

            // Erweiterte Konfiguration
            setDefaultValue(config, "max_transactions_per_account", 10);
            setDefaultValue(config, "min_pin", 1000);
            setDefaultValue(config, "max_pin", 9999);
            setDefaultValue(config, "max_balance", 1000000.0);

            plugin.saveConfig();
            LOGGER.info("Default configuration created successfully");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create default configuration", e);
            throw new RuntimeException("Configuration creation failed", e);
        }
    }

    private void setDefaultValue(FileConfiguration config, String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
            LOGGER.fine("Set default value for " + path + ": " + value);
        }
    }

    public boolean existsConfig() {
        try {
            FileConfiguration config = plugin.getConfig();

            // Prüfe ob grundlegende Konfiguration existiert
            return config.contains("database.type") &&
                    config.contains("prefix") &&
                    config.contains("currency_symbol");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking config existence", e);
            return false;
        }
    }

    public boolean existsSendData() {
        return plugin.getConfig().contains("send_anonymous_data");
    }

    public boolean existsBedrockSupport() {
        return plugin.getConfig().contains("bedrock_support");
    }

    public boolean isConfigurationValid() {
        try {
            FileConfiguration config = plugin.getConfig();

            // Prüfe Database-Konfiguration
            String dbType = getDBType();
            if (dbType == null) {
                LOGGER.warning("Database type is not configured");
                return false;
            }

            if ("mysql".equalsIgnoreCase(dbType)) {
                if (!isValidMySQLConfiguration()) {
                    return false;
                }
            } else if ("sqlite".equalsIgnoreCase(dbType)) {
                String dbFile = getDBFile();
                if (dbFile == null || dbFile.trim().isEmpty()) {
                    LOGGER.warning("SQLite database file path is not configured");
                    return false;
                }
            } else {
                LOGGER.warning("Unknown database type: " + dbType);
                return false;
            }

            // Prüfe andere kritische Werte
            if (getPrefix() == null || getPrefix().trim().isEmpty()) {
                LOGGER.warning("Plugin prefix is not configured");
                return false;
            }

            if (getSymbol() == null || getSymbol().trim().isEmpty()) {
                LOGGER.warning("Currency symbol is not configured");
                return false;
            }

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating configuration", e);
            return false;
        }
    }

    private boolean isValidMySQLConfiguration() {
        String host = getDBHost();
        String username = getDBUsername();
        String password = getDBPassword();
        String database = getDBDatabase();
        int port = getDBPort();

        if (host == null || host.trim().isEmpty() || MYSQL_PLACEHOLDERS.contains(host)) {
            LOGGER.warning("MySQL host is not properly configured");
            return false;
        }

        if (username == null || username.trim().isEmpty() || MYSQL_PLACEHOLDERS.contains(username)) {
            LOGGER.warning("MySQL username is not properly configured");
            return false;
        }

        if (password == null || password.trim().isEmpty() || MYSQL_PLACEHOLDERS.contains(password)) {
            LOGGER.warning("MySQL password is not properly configured");
            return false;
        }

        if (database == null || database.trim().isEmpty() || MYSQL_PLACEHOLDERS.contains(database)) {
            LOGGER.warning("MySQL database name is not properly configured");
            return false;
        }

        if (port <= 0 || port > 65535) {
            LOGGER.warning("MySQL port is not valid: " + port);
            return false;
        }

        return true;
    }

    // Getter-Methoden mit Validierung und Default-Werten
    public String getPrefix() {
        String prefix = plugin.getConfig().getString("prefix", DEFAULT_PREFIX);
        return prefix != null ? prefix : DEFAULT_PREFIX;
    }

    public boolean isSendDataEnabled() {
        return plugin.getConfig().getBoolean("send_anonymous_data", true);
    }

    public boolean isBedrockSupportEnabled() {
        return plugin.getConfig().getBoolean("bedrock_support", false);
    }

    public int getBankCost() {
        int cost = plugin.getConfig().getInt("bank_account_cost", DEFAULT_BANK_ACCOUNT_COST);
        return Math.max(0, cost); // Stelle sicher, dass Kosten nicht negativ sind
    }

    public String getSymbol() {
        String symbol = plugin.getConfig().getString("currency_symbol", DEFAULT_CURRENCY_SYMBOL);
        return symbol != null && !symbol.trim().isEmpty() ? symbol : DEFAULT_CURRENCY_SYMBOL;
    }

    public int getCardCost() {
        int cost = plugin.getConfig().getInt("new_card_cost", DEFAULT_CARD_COST);
        return Math.max(0, cost); // Stelle sicher, dass Kosten nicht negativ sind
    }

    // Database Getter - KORRIGIERT: Verwendung der richtigen Config-Keys
    public String getDBPassword() {
        return plugin.getConfig().getString("database.password");
    }

    public int getDBPort() {
        int port = plugin.getConfig().getInt("database.port", DEFAULT_DB_PORT);
        return (port > 0 && port <= 65535) ? port : DEFAULT_DB_PORT;
    }

    public String getDBUsername() {
        return plugin.getConfig().getString("database.username");
    }

    public String getDBHost() {
        return plugin.getConfig().getString("database.host");
    }

    public String getDBDatabase() {
        return plugin.getConfig().getString("database.database");
    }

    public String getDBFile() {
        String file = plugin.getConfig().getString("database.file", DEFAULT_DB_FILE);
        if (file != null && !file.trim().isEmpty()) {
            // Stelle sicher, dass das Verzeichnis existiert
            try {
                File dbFile = new File(file);
                File parentDir = dbFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    if (parentDir.mkdirs()) {
                        LOGGER.info("Created database directory: " + parentDir.getAbsolutePath());
                    } else {
                        LOGGER.warning("Failed to create database directory: " + parentDir.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error handling database file path: " + file, e);
            }
        }
        return file != null ? file : DEFAULT_DB_FILE;
    }

    public String getDBType() {
        String type = plugin.getConfig().getString("database.type", "sqlite");
        return type != null ? type.toLowerCase() : "sqlite";
    }

    public boolean isAutoUpdateEnabled() {
        return plugin.getConfig().getBoolean("auto_update", false);
    }

    // Erweiterte Konfigurationsoptionen
    public int getMaxTransactionsPerAccount() {
        int max = plugin.getConfig().getInt("max_transactions_per_account", 10);
        return Math.max(1, Math.min(max, 100)); // Zwischen 1 und 100
    }

    public int getMinPin() {
        return plugin.getConfig().getInt("min_pin", 1000);
    }

    public int getMaxPin() {
        return plugin.getConfig().getInt("max_pin", 9999);
    }

    public double getMaxBalance() {
        double max = plugin.getConfig().getDouble("max_balance", 1000000.0);
        return Math.max(0, max);
    }

    // Utility-Methoden
    public void reloadConfig() {
        try {
            plugin.reloadConfig();
            LOGGER.info("Configuration reloaded successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to reload configuration", e);
            throw new RuntimeException("Configuration reload failed", e);
        }
    }

    public boolean updateConfigValue(String path, Object value) {
        try {
            plugin.getConfig().set(path, value);
            plugin.saveConfig();
            LOGGER.info("Updated config value: " + path + " = " + value);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to update config value: " + path, e);
            return false;
        }
    }

    public void createBackupConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (configFile.exists()) {
                File backupFile = new File(plugin.getDataFolder(),
                        "config_backup_" + System.currentTimeMillis() + ".yml");

                if (configFile.renameTo(backupFile)) {
                    LOGGER.info("Configuration backup created: " + backupFile.getName());
                } else {
                    LOGGER.warning("Failed to create configuration backup");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating configuration backup", e);
        }
    }
}