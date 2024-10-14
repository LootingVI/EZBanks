package de.flori.ezbanks;

import de.flori.ezbanks.commands.BankCommand;
import de.flori.ezbanks.commands.ChangePinCommand;
import de.flori.ezbanks.commands.HelpCommand;
import de.flori.ezbanks.config.ConfigManager;
import de.flori.ezbanks.database.DatabaseManager;
import de.flori.ezbanks.events.PlayerInteractEvent;
import de.flori.ezbanks.gui.BankAccountGUI;
import de.flori.ezbanks.gui.BankMenuGUI;
import de.flori.ezbanks.gui.BuyCardGUI;
import de.flori.ezbanks.gui.BuybankAccountGUI;
import de.flori.ezbanks.manager.BankManager;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class EZBanks extends JavaPlugin {

    @Getter
    private static EZBanks instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private BankManager bankManager;
    private Economy economy;
    private static final String SPIGOT_RESOURCE_ID = "119092";

    @Override
    public void onEnable() {
        instance = this;


        getLogger().severe("EZBanks - Remember, this is the first alpha of the plugin it does not contain all functions yet! If you find any bugs please let me know on SpigotMC.");

        //if (!setupEconomy()) {
            //getLogger().severe("Vault not found! Disabling plugin...");
            //getServer().getPluginManager().disablePlugin(this);
            //return;
        //}

        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        economy = rsp.getProvider();

        configManager = new ConfigManager();
        if (!configManager.existsConfig()) configManager.createConfig();


        databaseManager = new DatabaseManager(configManager.getDBHost(), configManager.getDBPort(), configManager.getDBUsername(), configManager.getDBPassword(), configManager.getDBDatabase(), "EZBank");
        bankManager = new BankManager();

        registerCommands();
        registerListeners();
    }

    private void registerCommands() {
        final CommandMap commandMap = getServer().getCommandMap();
        commandMap.register("bank", new BankCommand());
        commandMap.register("setpin", new ChangePinCommand());
        commandMap.register("bankhelp", new HelpCommand());
    }

    private void registerListeners() {
        final PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new BankAccountGUI(), this);
        manager.registerEvents(new BankMenuGUI(), this);
        manager.registerEvents(new BuyCardGUI(), this);
        manager.registerEvents(new BuybankAccountGUI(), this);
        manager.registerEvents(new PlayerInteractEvent(), this);
    }

    private boolean setupEconomy() {
        //if (getServer().getPluginManager().getPlugin("Vault") == null) return false;

        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        economy = rsp.getProvider();
        return true;
    }

    public static String getPrefix() {
        return EZBanks.getInstance().getConfigManager().getPrefix();
    }

}
