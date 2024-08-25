package de.flori.ezbanks;

import de.flori.ezbanks.commands.BankCommand;
import de.flori.ezbanks.commands.ChangePinCommand;
import de.flori.ezbanks.commands.HelpCommand;
import de.flori.ezbanks.config.ConfigManager;
import de.flori.ezbanks.database.DatabaseManager;
import de.flori.ezbanks.events.EventBummlux;
import de.flori.ezbanks.events.EventDingens;
import de.flori.ezbanks.events.InventoryInteraction;
import de.flori.ezbanks.manager.BankManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Generated;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class EZBanks extends JavaPlugin {

    @Getter
    private static EZBanks instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private BankManager bankManager;
    private static Economy econ = null;



    @Override
    public void onEnable() {
        getLogger().severe(String.format("EZBanks -Remember, this is the first alpha of the plugin it does not contain all functions yet! If you find any bugs please let me know on SpigotMC."));
        setupEconomy();

        /*

        if (!setupEconomy() ) {
          getLogger().severe(String.format("EZBanks - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
         */

        getServer().getPluginManager().registerEvents(new InventoryInteraction(), this);
        getServer().getPluginManager().registerEvents(new EventBummlux(), this);
        getServer().getPluginManager().registerEvents(new EventDingens(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("bank", "Open ur bank menu", new BankCommand());
            commands.register("setpin", "Set a new pin for u bank account", new ChangePinCommand());
            commands.register("bankhelp", "Help with bank commands", new HelpCommand());
        });


        instance = this;

        configManager = new ConfigManager(this);

        if(configManager.getConfigP() == null) {
            configManager.createMYSQConfig();
        }
        databaseManager = new DatabaseManager(configManager.getDBIP(), configManager.getDBPort(), configManager.getDBUsername(), configManager.getDBPassword(), configManager.getDBDatabse(), "EZBank");
        bankManager = new BankManager();

    }

    @Override
    public void onDisable() {
        //Bukkit.getScheduler().runTaskAsynchronously(this, () -> databaseManager.disconnect());
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }


    @Generated
    public static EZBanks getInstance() {
        return instance;
    }

    @Generated
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Generated
    public BankManager bankManager() {
        return bankManager;
    }
    @Generated
    public ConfigManager configManager() {
        return configManager;
    }



}
