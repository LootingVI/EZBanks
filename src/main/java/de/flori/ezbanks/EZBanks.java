package de.flori.ezbanks;

import de.flori.ezbanks.commands.BankCommand;
import de.flori.ezbanks.commands.ChangePinCommand;
import de.flori.ezbanks.commands.HelpCommand;
import de.flori.ezbanks.commands.SuspendCommand;
import de.flori.ezbanks.config.ConfigManager;
import de.flori.ezbanks.database.DatabaseManager;
import de.flori.ezbanks.events.PlayerInteractEvent;
import de.flori.ezbanks.gui.BankAccountGUI;
import de.flori.ezbanks.gui.BankMenuGUI;
import de.flori.ezbanks.gui.BuyCardGUI;
import de.flori.ezbanks.gui.BuybankAccountGUI;
import de.flori.ezbanks.manager.BankManager;
import de.flori.ezbanks.manager.impl.DatabaseType;
import de.flori.ezbanks.utils.PluginUpdater;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter
public final class EZBanks extends JavaPlugin {

    @Getter
    private static EZBanks instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private BankManager bankManager;
    private Economy economy;
    private FloodgateApi floodgateApi;
    private ExecutorService asyncExecutor;

    private boolean initializationComplete = false;

    @Override
    public void onEnable() {
        instance = this;
        asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "EZBanks-Async");
            thread.setDaemon(true);
            return thread;
        });

        try {
            initializePlugin();
            getLogger().info("EZBanks has been successfully enabled!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize EZBanks plugin", e);
            disablePlugin("Plugin initialization failed");
            return;
        }
    }

    private void initializePlugin() {
        logWelcomeMessages();

        // Lade Konfiguration
        if (!initializeConfiguration()) {
            throw new RuntimeException("Failed to initialize configuration");
        }

        // Initialisiere Datenbank
        if (!initializeDatabase()) {
            throw new RuntimeException("Failed to initialize database");
        }

        // Initialisiere Manager
        if (!initializeManagers()) {
            throw new RuntimeException("Failed to initialize managers");
        }

        // Initialisiere externe Abhängigkeiten
        initializeExternalDependencies();

        // Registriere Commands und Events
        registerComponents();

        // Initialisiere optionale Features
        initializeOptionalFeatures();

        initializationComplete = true;
    }

    private void logWelcomeMessages() {
        getLogger().info("=== EZBanks Plugin Starting ===");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Support Discord: https://discord.gg/k4knhZrTYt");
        getLogger().warning("This is an alpha version - please report bugs on our Discord server!");
    }

    private boolean initializeConfiguration() {
        try {
            configManager = new ConfigManager();

            if (!configManager.existsConfig()) {
                getLogger().info("Creating default configuration...");
                configManager.createConfig();
            }

            // Stelle sicher, dass alle notwendigen Config-Keys existieren
            ensureConfigDefaults();

            getLogger().info("Configuration loaded successfully");
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize configuration", e);
            return false;
        }
    }

    private void ensureConfigDefaults() {
        boolean configChanged = false;

        if (!configManager.existsSendData()) {
            getConfig().set("send_anonymous_data", true);
            configChanged = true;
            getLogger().info("Added default value for send_anonymous_data");
        }

        if (!configManager.existsBedrockSupport()) {
            getConfig().set("bedrock_support", false);
            configChanged = true;
            getLogger().info("Added default value for bedrock_support");
        }

        // Weitere Default-Werte können hier hinzugefügt werden
        if (!getConfig().contains("auto_update")) {
            getConfig().set("auto_update", false);
            configChanged = true;
            getLogger().info("Added default value for auto_update");
        }

        if (configChanged) {
            saveConfig();
            getLogger().info("Configuration updated with default values");
        }
    }

    private boolean initializeDatabase() {
        try {
            String dbTypeStr = configManager.getDBType();
            if (dbTypeStr == null || dbTypeStr.trim().isEmpty()) {
                getLogger().warning("Database type not specified, defaulting to SQLite");
                dbTypeStr = "sqlite";
            }

            DatabaseType dbType = dbTypeStr.equalsIgnoreCase("mysql") ? DatabaseType.MYSQL : DatabaseType.SQLITE;
            getLogger().info("Initializing " + dbType.name() + " database...");

            if (dbType == DatabaseType.SQLITE) {
                String fileName = configManager.getDBFile();
                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = "ezbanks.db";
                    getLogger().warning("Database file not specified, using default: " + fileName);
                }

                this.databaseManager = new DatabaseManager(dbType, null, 0, null, null, fileName, null);
            } else {
                // MySQL Konfiguration mit Validierung
                String host = getConfig().getString("database.host");
                int port = getConfig().getInt("database.port", 3306);
                String username = getConfig().getString("database.username");
                String password = getConfig().getString("database.password");
                String database = getConfig().getString("database.database");

                if (host == null || username == null || password == null || database == null) {
                    throw new IllegalArgumentException("MySQL configuration incomplete. Required: host, username, password, database");
                }

                this.databaseManager = new DatabaseManager(dbType, host, port, username, password, database, "EZBanksPool");
            }

            // Teste Datenbankverbindung
            if (!databaseManager.isConnected()) {
                throw new RuntimeException("Database connection test failed");
            }

            getLogger().info("Database initialized successfully");
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            return false;
        }
    }

    private boolean initializeManagers() {
        try {
            getLogger().info("Initializing bank manager...");
            bankManager = new BankManager();
            getLogger().info("Bank manager initialized successfully");
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize bank manager", e);
            return false;
        }
    }

    private void initializeExternalDependencies() {
        // Vault/Economy Setup
        if (!setupEconomy()) {
            getLogger().warning("Economy system not available - some features may not work");
        } else {
            getLogger().info("Economy system initialized successfully");
        }

        // Floodgate Setup
        if (configManager.isBedrockSupportEnabled()) {
            setupFloodgate();
        }
    }

    private boolean setupEconomy() {
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            getLogger().severe("Vault plugin is required but not found!");
            getLogger().severe("Please install Vault: https://www.spigotmc.org/resources/vault.34315/");
            return false;
        }

        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No economy plugin found that supports Vault!");
            getLogger().severe("Please install an economy plugin like EssentialsX");
            return false;
        }

        economy = rsp.getProvider();
        getLogger().info("Hooked into economy provider: " + economy.getName());
        return true;
    }

    private void setupFloodgate() {
        if (!getServer().getPluginManager().isPluginEnabled("floodgate")) {
            getLogger().warning("Bedrock support is enabled but Floodgate plugin is not installed!");
            getLogger().warning("Please install Floodgate or disable bedrock_support in config");
            return;
        }

        try {
            floodgateApi = FloodgateApi.getInstance();
            getLogger().info("Floodgate integration enabled successfully");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to initialize Floodgate integration", e);
        }
    }

    private void registerComponents() {
        try {
            registerCommands();
            registerListeners();
            getLogger().info("Commands and listeners registered successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register components", e);
            throw new RuntimeException("Component registration failed", e);
        }
    }

    private void registerCommands() {
        final CommandMap commandMap = getServer().getCommandMap();

        try {
            commandMap.register("bank", new BankCommand());
            commandMap.register("setpin", new ChangePinCommand());
            commandMap.register("bankhelp", new HelpCommand());
            commandMap.register("bank-suspend", new SuspendCommand());
            getLogger().fine("All commands registered successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register commands", e);
            throw e;
        }
    }

    private void registerListeners() {
        final PluginManager manager = getServer().getPluginManager();

        try {
            manager.registerEvents(new BankAccountGUI(), this);
            manager.registerEvents(new BankMenuGUI(), this);
            manager.registerEvents(new BuyCardGUI(), this);
            manager.registerEvents(new BuybankAccountGUI(), this);
            manager.registerEvents(new PlayerInteractEvent(), this);
            getLogger().fine("All event listeners registered successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register event listeners", e);
            throw e;
        }
    }

    private void initializeOptionalFeatures() {
        // bStats Metriken
        if (configManager.isSendDataEnabled()) {
            try {
                final int pluginId = 23630;
                new Metrics(this, pluginId);
                getLogger().info("Anonymous statistics enabled (bStats)");
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to initialize bStats metrics", e);
            }
        } else {
            getLogger().info("Anonymous statistics disabled");
        }

        // Auto-Update
        if (configManager.isAutoUpdateEnabled()) {
            initializeAutoUpdater();
        } else {
            getLogger().info("Auto-updater disabled");
        }
    }

    private void initializeAutoUpdater() {
        CompletableFuture.runAsync(() -> {
            try {
                getLogger().info("Checking for plugin updates...");
                new PluginUpdater(this.getFile(), this.getLogger(), this.getDescription().getVersion())
                        .checkAndUpdatePlugin();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Auto-update check failed", e);
            }
        }, asyncExecutor);
    }

    @Override
    public void onDisable() {
        try {
            getLogger().info("Shutting down EZBanks...");

            // Schließe Datenbankverbindungen
            if (databaseManager != null) {
                try {
                    databaseManager.disconnect();
                    getLogger().info("Database connections closed");
                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "Error closing database connections", e);
                }
            }

            // Schließe Thread Pool
            if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
                asyncExecutor.shutdown();
                try {
                    if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        asyncExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    asyncExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            getLogger().info("EZBanks disabled successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin shutdown", e);
        } finally {
            instance = null;
        }
    }

    private void disablePlugin(String reason) {
        getLogger().severe("Disabling plugin: " + reason);
        getServer().getPluginManager().disablePlugin(this);
    }

    // Utility methods
    public static String getPrefix() {
        EZBanks instance = EZBanks.getInstance();
        return instance != null && instance.getConfigManager() != null ?
                instance.getConfigManager().getPrefix() : "[EZBanks] ";
    }

    public static boolean isBedrockSupportAvailable() {
        EZBanks instance = EZBanks.getInstance();
        return instance != null &&
                instance.getConfigManager() != null &&
                instance.getConfigManager().isBedrockSupportEnabled() &&
                instance.getFloodgateApi() != null;
    }

    public boolean isInitializationComplete() {
        return initializationComplete;
    }

    public ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    // Graceful reload support
    public CompletableFuture<Boolean> reloadConfiguration() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                getLogger().info("Reloading configuration...");
                reloadConfig();

                // Re-initialize configuration manager
                configManager = new ConfigManager();
                ensureConfigDefaults();

                getLogger().info("Configuration reloaded successfully");
                return true;
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to reload configuration", e);
                return false;
            }
        }, asyncExecutor);
    }
}