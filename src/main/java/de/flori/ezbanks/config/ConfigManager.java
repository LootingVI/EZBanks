package de.flori.ezbanks.config;

import de.flori.ezbanks.EZBanks;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    public ConfigManager() {
    }

    public void createConfig() {
        FileConfiguration configuration = EZBanks.getInstance().getConfig();
        configuration.set("database.type", "sqlite");
        configuration.set("database.password", "Type ur MYSQL password here");
        configuration.set("database.name", "Type ur MYSQL username here");
        configuration.set("database.ip", "Type ur MYSQL ip here");
        configuration.set("database.port", 3306);
        configuration.set("database.database", "Type ur MYSQL database name here");
        configuration.set("database.file", "plugins/EzBanks/database.db");
        configuration.set("prefix", "§cEZBank §7>> ");
        configuration.set("currency_symbol", "$");
        configuration.set("new_card_cost", 1000);
        configuration.set("bank_account_cost", 0);
        configuration.set("send_anonymous_data", true);
        configuration.set("bedrock_support", false);
        configuration.set("auto_update", true);
        EZBanks.getInstance().saveConfig();
    }

    public boolean existsConfig() {
        return EZBanks.getInstance().getConfig().getString("database.ip") != null;
    }

    public boolean existsSendData() {
        return EZBanks.getInstance().getConfig().getString("send_anonymous_data") != null;
    }

    public boolean existsBedrockSupport() {
        return EZBanks.getInstance().getConfig().getString("bedrock_support") != null;
    }

    public String getPrefix() {
        return EZBanks.getInstance().getConfig().getString("prefix");
    }

    public boolean isSendDataEnabled() {
        return EZBanks.getInstance().getConfig().getBoolean("send_anonymous_data");
    }

    public boolean isBedrockSupportEnabled() {
        return EZBanks.getInstance().getConfig().getBoolean("bedrock_support");
    }

    public int getBankCost() {
        return EZBanks.getInstance().getConfig().getInt("bank_account_cost");
    }

    public String getSymbol() {
        return EZBanks.getInstance().getConfig().getString("currency_symbol");
    }

    public int getCardCost() {
        return EZBanks.getInstance().getConfig().getInt("new_card_cost");
    }

    public String getDBPassword() {
        return EZBanks.getInstance().getConfig().getString("MYSQL.password");
    }

    public int getDBPort() {
        return EZBanks.getInstance().getConfig().getInt("database.port");
    }

    public String getDBUsername() {
        return EZBanks.getInstance().getConfig().getString("database.name");
    }

    public String getDBHost() {
        return EZBanks.getInstance().getConfig().getString("database.ip");
    }

    public String getDBDatabase() {
        return EZBanks.getInstance().getConfig().getString("database.database");
    }

    public String getDBFile() {
        return EZBanks.getInstance().getConfig().getString("database.file");
    }

    public String getDBType() {
        return EZBanks.getInstance().getConfig().getString("database.type");
    }

    public boolean isAutoUpdateEnabled() {
        return EZBanks.getInstance().getConfig().getBoolean("auto_update");
    }
}
