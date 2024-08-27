package de.flori.ezbanks.manager;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.database.DatabaseManager;
import de.flori.ezbanks.manager.impl.BankAccount;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BankManager {

    private final DatabaseManager databaseManager;

    public BankManager() {
        this.databaseManager = EZBanks.getInstance().getDatabaseManager();
        databaseManager.update("CREATE TABLE IF NOT EXISTS `banks` (`id` VARCHAR(10) NOT NULL PRIMARY KEY, `ownerUuid` UUID NOT NULL, `balance` DOUBLE NOT NULL, `suspended` INT NOT NULL, `pin` INT NOT NULL) ENGINE = InnoDB;");
    }

    public void createBankAccount(BankAccount account) {
        databaseManager.update("INSERT INTO `banks` (`id`, `ownerUuid`, `balance`, `suspended`, `pin`) VALUES ('" + account.getBankId() + "', '" + account.getOwnerUuid() + "', '" + account.getBalance() + "', '" + (account.isSuspended() ? "1" : "0") + "', '" + account.getPin() + "');");
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

            return new BankAccount(
                    resultSet.getString("id"),
                    UUID.fromString(resultSet.getString("ownerUuid")),
                    resultSet.getDouble("balance"),
                    resultSet.getInt("pin"),
                    resultSet.getInt("suspended") == 1
            );
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public BankAccount getBankAccount(UUID ownerUuid) {
        try (ResultSet resultSet = databaseManager.getResult("SELECT * FROM `banks` WHERE ownerUuid='" + ownerUuid + "'")) {
            if (!resultSet.next()) return null;

            return new BankAccount(
                    resultSet.getString("id"),
                    UUID.fromString(resultSet.getString("ownerUuid")),
                    resultSet.getDouble("balance"),
                    resultSet.getInt("pin"),
                    resultSet.getInt("suspended") == 1
            );
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public void addBalance(BankAccount account, double amount) {
        final double currentBalance = account.getBalance();
        final double newBalance = currentBalance + amount;

        databaseManager.update("UPDATE `banks` SET `balance` = '" + newBalance + "' WHERE `id` = '" + account.getBankId() + "'; ");
    }

    public void changePin(BankAccount account, Integer pin){
        databaseManager.update("UPDATE `banks` SET `pin` = '" + pin + "' WHERE `id` = '" + account.getBankId() + "'; ");
    }

    public void removeBalance(BankAccount account, double amount){
        final double currentBalance = account.getBalance();
        final double newBalance = currentBalance - amount;

        databaseManager.update("UPDATE `banks` SET `balance` = '" + newBalance + "' WHERE `id` = '" + account.getBankId() + "'; ");
    }

}
