package de.flori.ezbanks.config;

import de.flori.ezbanks.EZBanks;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    public void createConfig(){
        final FileConfiguration configuration = EZBanks.getInstance().getConfig();
        configuration.set("MYSQL.password", "Type ur MYSQL password here");
        configuration.set("MYSQL.name", "Type ur MYSQL username here");
        configuration.set("MYSQL.ip", "Type ur MYSQL ip here");
        configuration.set("MYSQL.port", 3306);
        configuration.set("MYSQL.database", "Type ur MYSQL username here");
        configuration.set("prefix", "§cEZBank §7>> ");
        configuration.set("currency_symbol", "$");
        configuration.set("new_card_cost", 1000);
        configuration.set("bank_account_cost", 0);
        configuration.set("send_anonymous_data", true);
        configuration.set("bedrock_support", false);

        EZBanks.getInstance().saveConfig();
    }

    public boolean existsConfig(){
        return EZBanks.getInstance().getConfig().getString("MYSQL.ip") != null;
    }

    public boolean existsSendData() {
        return EZBanks.getInstance().getConfig().getString("send_anonymous_data") != null;
    }

    public boolean existsBedrockSupport() {
        return EZBanks.getInstance().getConfig().getString("bedrock_support") != null;
    }

    public String getPrefix(){
        return EZBanks.getInstance().getConfig().getString("prefix");
    }

    public boolean isSendDataEnabled(){
        return EZBanks.getInstance().getConfig().getBoolean("send_anonymous_data");
    }

    public boolean isBedrockSupportEnabled(){
        return EZBanks.getInstance().getConfig().getBoolean("bedrock_support");
    }

    public int getBankCost(){
        return  EZBanks.getInstance().getConfig().getInt("bank_account_cost");
    }

    public String getSymbol(){
        return EZBanks.getInstance().getConfig().getString("currency_symbol");
    }

    public int getCardCost(){
        return EZBanks.getInstance().getConfig().getInt("new_card_cost");
    }

    public String getDBPassword(){
        return EZBanks.getInstance().getConfig().getString("MYSQL.password");
    }

    public int getDBPort(){
        return EZBanks.getInstance().getConfig().getInt("MYSQL.port");
    }

    public String getDBUsername(){
        return EZBanks.getInstance().getConfig().getString("MYSQL.name");
    }

    public String getDBHost(){
        return EZBanks.getInstance().getConfig().getString("MYSQL.ip");
    }

    public String getDBDatabase(){
        return EZBanks.getInstance().getConfig().getString("MYSQL.database");
    }


}
