package de.flori.ezbanks.config;

import de.flori.ezbanks.EZBanks;

public class ConfigManager {

    EZBanks main;

    public ConfigManager(EZBanks main){
        this.main = main;
    }

    public void createMYSQConfig(){
        main.getConfig().set("MYSQL.password", "Type ur MYSQL password here");
        main.getConfig().set("MYSQL.name", "Type ur MYSQL username here");
        main.getConfig().set("MYSQL.ip", "Type ur MYSQL ip here");
        main.getConfig().set("MYSQL.port", 3306);
        main.getConfig().set("MYSQL.database", "Type ur MYSQL username here");
        main.getConfig().set("prefix", "§cEZBank §7>> ");
        main.getConfig().set("currency_symbol", "$");
        main.getConfig().set("new_card_cost", 1000);
        main.saveConfig();
    }

    public String getConfigP(){
        return main.getConfig().getString("MYSQL.ip");
    }

    public String getPrefix(){
        return main.getConfig().getString("prefix");
    }

    public String getSymbol(){
        return main.getConfig().getString("currency_symbol");
    }

    public Integer getCardCost(){
        return main.getConfig().getInt("new_card_cost");
    }


    public String getDBPassword(){
        return main.getConfig().getString("MYSQL.password");
    }

    public Integer getDBPort(){
        return main.getConfig().getInt("MYSQL.port");
    }


    public String getDBUsername(){
        return main.getConfig().getString("MYSQL.name");
    }

    public String getDBIP(){
        return main.getConfig().getString("MYSQL.ip");
    }

    public String getDBDatabse(){
        return main.getConfig().getString("MYSQL.database");
    }


}
