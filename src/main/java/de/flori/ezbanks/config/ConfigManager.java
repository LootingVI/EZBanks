package de.flori.ezbanks.config;

import de.flori.ezbanks.EZBanks;

public class ConfigManager {

    public void createConfig(){
        EZBanks.getInstance().getConfig().set("MYSQL.password", "Type ur MYSQL password here");
        EZBanks.getInstance().getConfig().set("MYSQL.name", "Type ur MYSQL username here");
        EZBanks.getInstance().getConfig().set("MYSQL.ip", "Type ur MYSQL ip here");
        EZBanks.getInstance().getConfig().set("MYSQL.port", 3306);
        EZBanks.getInstance().getConfig().set("MYSQL.database", "Type ur MYSQL username here");
        EZBanks.getInstance().getConfig().set("prefix", "§cEZBank §7>> ");
        EZBanks.getInstance().getConfig().set("currency_symbol", "$");
        EZBanks.getInstance().getConfig().set("new_card_cost", 1000);
        EZBanks.getInstance().getConfig().set("bank_account_cost", 0);
        EZBanks.getInstance().getConfig().set("send_anonym_data", true);
        EZBanks.getInstance().saveConfig();
    }

    public boolean existsConfig(){
        return EZBanks.getInstance().getConfig().getString("MYSQL.ip") != null;
    }

    public boolean existsSendData(){
        return EZBanks.getInstance().getConfig().getString("send_anonym_data") != null;
    }


    public String getPrefix(){
        return EZBanks.getInstance().getConfig().getString("prefix");
    }

    public boolean isSendDataEnable(){
        return EZBanks.getInstance().getConfig().getBoolean("send_anonym_data");
    }


    public Integer getBankCost(){
        return  EZBanks.getInstance().getConfig().getInt("bank_account_cost");
    }

    public String getSymbol(){
        return EZBanks.getInstance().getConfig().getString("currency_symbol");
    }

    public Integer getCardCost(){
        return EZBanks.getInstance().getConfig().getInt("new_card_cost");
    }

    public String getDBPassword(){
        return EZBanks.getInstance().getConfig().getString("MYSQL.password");
    }

    public Integer getDBPort(){
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
