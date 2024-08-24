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
        databaseManager.update("CREATE TABLE IF NOT EXISTS `bank` (`bid` VARCHAR(10) NOT NULL PRIMARY KEY, `ownerid` UUID NOT NULL, `balance` DOUBLE NOT NULL, `suspended` INT NOT NULL, `pin` INT NOT NULL) ENGINE = InnoDB;");
    }

    public void createBankAccount(BankAccount account) {
        databaseManager.update("INSERT INTO `bank` (`bid`, `ownerid`, `balance`, `suspended`, `pin`) VALUES ('" + account.getBankId() + "', '" + account.getOwnerId() + "', '" + account.getBalance() + "', '" + (account.isSuspended() ? "1" : "0") + "', '" + account.getPin() + "');");
    }

    public BankAccount getBankAccount(String bankId) {
        try (ResultSet resultSet = databaseManager.getResult("SELECT * FROM `bank` WHERE bid='" + bankId + "'")) {
            if (!resultSet.next())
                return null;

            return new BankAccount(
                    resultSet.getString("bid"),
                    UUID.fromString(resultSet.getString("ownerid")),
                    resultSet.getDouble("balance"),
                    resultSet.getInt("pin"),
                    resultSet.getInt("suspended") == 1
            );
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public BankAccount getBankAccount(UUID ownerID) {
        try (ResultSet resultSet = databaseManager.getResult("SELECT * FROM `bank` WHERE ownerid='" + ownerID + "'")) {
            if (!resultSet.next())
                return null;

            return new BankAccount(
                    resultSet.getString("bid"),
                    UUID.fromString(resultSet.getString("ownerid")),
                    resultSet.getDouble("balance"),
                    resultSet.getInt("pin"),
                    resultSet.getInt("suspended") == 1
            );
        } catch (SQLException exception) {
            exception.printStackTrace(new PrintStream(System.err));
            return null;
        }
    }

    public void addBalance(String bankId, Double moneyToAdd){

        Double oldBalance = getBankAccount(bankId).getBalance();
        Double newBalance = oldBalance + moneyToAdd;

        databaseManager.update("UPDATE `bank` SET `balance` = '" + newBalance + "' WHERE `bank`.`bid` = '" + bankId+ "'; ");

    }
    public void setNewPin(String bankId, Integer pin){

        databaseManager.update("UPDATE `bank` SET `pin` = '" + pin + "' WHERE `bank`.`bid` = '" + bankId+ "'; ");

    }


    public void removeBalance(String bankId, Double moneyToRemove){

        Double oldBalance = getBankAccount(bankId).getBalance();
        Double newBalance = oldBalance - moneyToRemove;

        databaseManager.update("UPDATE `bank` SET `balance` = '" + newBalance + "' WHERE `bank`.`bid` = '" + bankId+ "'; ");

    }

}
