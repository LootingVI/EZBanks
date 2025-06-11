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

@Getter
public final class EZBanks extends JavaPlugin {

    @Getter
    private static EZBanks instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private BankManager bankManager;
    private Economy economy;
    private FloodgateApi floodgateApi;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().severe("Support Discord Server: https://discord.gg/k4knhZrTYt");
        getLogger().severe("Remember, this is the first alpha of the plugin it does not contain all functions yet! If you find any bugs please let me know on our Discord server.");

        configManager = new ConfigManager();
        if (!configManager.existsConfig())
            configManager.createConfig();

        String type = configManager.getDBType();
        DatabaseType dbType = type.equalsIgnoreCase("mysql") ? DatabaseType.MYSQL : DatabaseType.SQLITE;
        String fileOrDbName = configManager.getDBFile();

        if (dbType == DatabaseType.SQLITE) {
            this.databaseManager = new DatabaseManager(dbType, null, 0, null, null, fileOrDbName, null);
        } else {
            String host = getConfig().getString("database.host");
            int port = getConfig().getInt("database.port");
            String user = getConfig().getString("database.username");
            String pass = getConfig().getString("database.password");
            String db = getConfig().getString("database.database");
            this.databaseManager = new DatabaseManager(dbType, host, port, user, pass, db, "EZBanksPool");
        }
        bankManager = new BankManager();

        setupEconomy();

        if(configManager.isBedrockSupportEnabled()){
            if(!getServer().getPluginManager().isPluginEnabled("floodgate")){
                getLogger().warning("Since you have activated bedrock support you have to install the plugin Floodgate so that EZBanks works without problems");
                return;
            }

            floodgateApi = FloodgateApi.getInstance();
            getLogger().info("Floodgate support has been initialized");
        }

        if(!configManager.existsSendData()){
            EZBanks.getInstance().getConfig().set("send_anonymous_data", true);
            EZBanks.getInstance().saveConfig();
        }
        if(!configManager.existsBedrockSupport()){
            EZBanks.getInstance().getConfig().set("bedrock_support", false);
            EZBanks.getInstance().saveConfig();
        }

        if(configManager.isSendDataEnabled()){
            final int pluginId = 23630;
            new Metrics(this, pluginId);
            getLogger().info("Sending of anonymous statistics to bStats successful");
        }

        registerCommands();
        registerListeners();

        if(configManager.isAutoUpdateEnabled()){
            new Thread(() -> {
                new PluginUpdater(this.getFile(), this.getLogger(), this.getDescription().getVersion()).checkAndUpdatePlugin();
            }).start();
        }

    }

    private void registerCommands() {
        final CommandMap commandMap = getServer().getCommandMap();
        commandMap.register("bank", new BankCommand());
        commandMap.register("setpin", new ChangePinCommand());
        commandMap.register("bankhelp", new HelpCommand());
        commandMap.register("bank-suspend", new SuspendCommand());
    }

    private void registerListeners() {
        final PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new BankAccountGUI(), this);
        manager.registerEvents(new BankMenuGUI(), this);
        manager.registerEvents(new BuyCardGUI(), this);
        manager.registerEvents(new BuybankAccountGUI(), this);
        manager.registerEvents(new PlayerInteractEvent(), this);
    }

    private void setupEconomy() {
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            System.err.println("You need to install the Vault plugin for EZBanks to work without problems");
            return;
        }

        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;

        economy = rsp.getProvider();
    }

    public static String getPrefix() {
        return EZBanks.getInstance().getConfigManager().getPrefix();
    }

    public static boolean isBedrockSupportAvailable() {
        return EZBanks.getInstance().getConfigManager().isBedrockSupportEnabled() && EZBanks.getInstance().getFloodgateApi() != null;
    }

}
