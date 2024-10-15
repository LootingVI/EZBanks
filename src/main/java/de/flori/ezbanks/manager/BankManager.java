package de.flori.ezbanks.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.database.DatabaseManager;
import de.flori.ezbanks.manager.enums.TransactionType;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.manager.impl.Transaction;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BankManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final DatabaseManager databaseManager;

    public BankManager() {
        this.databaseManager = EZBanks.getInstance().getDatabaseManager();
        databaseManager.update("CREATE TABLE IF NOT EXISTS `banks` (`id` VARCHAR(10) NOT NULL PRIMARY KEY, `ownerUuid` UUID NOT NULL, `data` TEXT) ENGINE = InnoDB;");
    }

    public void createBankAccount(BankAccount account) {
        databaseManager.update("INSERT INTO `banks` (`id`, `ownerUuid`, `data`) VALUES ('" + account.getBankId() + "', '" + account.getOwnerUuid() + "', '" + gson.toJson(account) + "');");
    }

    public boolean hasBankAccount(UUID ownerUuid) {
        try (ResultSet resultSet = databaseManager.getResult("SELECT * FROM `banks` WHERE ownerUuid='" + ownerUuid + "'")) {
            return resultSet.next();
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return false;
        }
    }

    public BankAccount getBankAccount(String bankId) {
        try (ResultSet resultSet = databaseManager.getResult("SELECT * FROM `banks` WHERE id='" + bankId + "'")) {
            if (!resultSet.next()) return null;
            return gson.fromJson(resultSet.getString("data"), BankAccount.class);
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public BankAccount getBankAccount(UUID ownerUuid) {
        try (ResultSet resultSet = databaseManager.getResult("SELECT * FROM `banks` WHERE ownerUuid='" + ownerUuid + "'")) {
            if (!resultSet.next()) return null;
            return gson.fromJson(resultSet.getString("data"), BankAccount.class);
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public void updateBankAccount(BankAccount account) {
        try {
            databaseManager.update("UPDATE `banks` SET data='" + gson.toJson(account) + "' WHERE id='" + account.getBankId() + "';");
        } catch (Exception exception) {
            exception.printStackTrace(new PrintStream(System.err));
        }
    }

    public void setNewPin(BankAccount account, int pin) {
        account.setPin(pin);
        updateBankAccount(account);
    }

    public void addBalance(BankAccount account, double amount) {
        account.setBalance(account.getBalance() + amount);
        updateBankAccount(account);
    }

    public void removeBalance(BankAccount account, double amount) {
        account.setBalance(account.getBalance() - amount);
        updateBankAccount(account);
    }

    public void addTransaction(BankAccount account, TransactionType type, double amount, UUID player) {
        account.getTransactions().add(new Transaction(type, amount, System.currentTimeMillis(), player));

        while (account.getTransactions().size() > 10)
            account.getTransactions().removeFirst();

        updateBankAccount(account);
    }

    public void setSuspended(BankAccount account, boolean in){
        account.setSuspended(in);

        updateBankAccount(account);
    }



}
