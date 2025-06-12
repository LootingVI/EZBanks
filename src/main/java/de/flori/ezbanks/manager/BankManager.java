package de.flori.ezbanks.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.database.DatabaseManager;
import de.flori.ezbanks.manager.enums.TransactionType;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.manager.impl.DatabaseType;
import de.flori.ezbanks.manager.impl.Transaction;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BankManager {
    private final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
    private final DatabaseManager databaseManager = EZBanks.getInstance().getDatabaseManager();

    public BankManager() {
        String createTable;
        if (this.databaseManager.getType() == DatabaseType.MYSQL) {
            createTable = "CREATE TABLE IF NOT EXISTS `banks` (`id` VARCHAR(10) NOT NULL PRIMARY KEY,`ownerUuid` VARCHAR(36) NOT NULL,`data` TEXT) ENGINE=InnoDB;";
        } else {
            createTable = "CREATE TABLE IF NOT EXISTS `banks` (`id` TEXT NOT NULL PRIMARY KEY,`ownerUuid` TEXT NOT NULL,`data` TEXT);";
        }

        this.databaseManager.update(createTable);
    }

    public void createBankAccount(BankAccount account) {
        DatabaseManager var10000 = this.databaseManager;
        String var10001 = account.getBankId();
        var10000.update("INSERT INTO `banks` (`id`, `ownerUuid`, `data`) VALUES ('" + var10001 + "', '" + String.valueOf(account.getOwnerUuid()) + "', '" + this.gson.toJson(account) + "');");
    }

    public boolean hasBankAccount(UUID ownerUuid) {
        try {
            boolean var3;
            try (ResultSet resultSet = this.databaseManager.getResult("SELECT * FROM `banks` WHERE ownerUuid='" + String.valueOf(ownerUuid) + "'")) {
                var3 = resultSet != null && resultSet.next();
            }

            return var3;
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return false;
        }
    }

    public BankAccount getBankAccount(String bankId) {
        try (ResultSet resultSet = this.databaseManager.getResult("SELECT * FROM `banks` WHERE id='" + bankId + "'")) {
            if (resultSet != null && resultSet.next()) {
                return (BankAccount)this.gson.fromJson(resultSet.getString("data"), BankAccount.class);
            } else {
                return null;
            }
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public BankAccount getBankAccount(UUID ownerUuid) {
        try (ResultSet resultSet = this.databaseManager.getResult("SELECT * FROM `banks` WHERE ownerUuid='" + String.valueOf(ownerUuid) + "'")) {
            if (resultSet != null && resultSet.next()) {
                return (BankAccount)this.gson.fromJson(resultSet.getString("data"), BankAccount.class);
            } else {
                return null;
            }
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public void updateBankAccount(BankAccount account) {
        try {
            DatabaseManager var10000 = this.databaseManager;
            String var10001 = this.gson.toJson(account);
            var10000.update("UPDATE `banks` SET data='" + var10001 + "' WHERE id='" + account.getBankId() + "';");
        } catch (Exception exception) {
            exception.printStackTrace(new PrintStream(System.err));
        }

    }

    public void setNewPin(BankAccount account, int pin) {
        account.setPin(pin);
        this.updateBankAccount(account);
    }

    public void addBalance(BankAccount account, double amount) {
        account.setBalance(account.getBalance() + amount);
        this.updateBankAccount(account);
    }

    public void removeBalance(BankAccount account, double amount) {
        account.setBalance(account.getBalance() - amount);
        this.updateBankAccount(account);
    }

    public void addTransaction(BankAccount account, TransactionType type, double amount, UUID player) {
        account.getTransactions().add(new Transaction(type, amount, System.currentTimeMillis(), player));

        while(account.getTransactions().size() > 5) {
            account.getTransactions().removeFirst();
        }

        this.updateBankAccount(account);
    }

    public void setSuspended(BankAccount account, boolean in) {
        account.setSuspended(in);
        this.updateBankAccount(account);
    }
}