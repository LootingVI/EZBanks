package de.flori.ezbanks.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.database.DatabaseManager;
import de.flori.ezbanks.manager.impl.DatabaseType;
import de.flori.ezbanks.manager.enums.TransactionType;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.manager.impl.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BankManager {

    private static final Logger LOGGER = Logger.getLogger(BankManager.class.getName());

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final DatabaseManager databaseManager;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final de.flori.ezbanks.config.ConfigManager configManager;

    public BankManager() {
        this.databaseManager = EZBanks.getInstance().getDatabaseManager();
        this.configManager = EZBanks.getInstance().getConfigManager();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            String createTable = buildCreateTableQuery();
            databaseManager.executeUpdate(createTable);
            LOGGER.info("Bank accounts table initialized successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize bank accounts table", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private String buildCreateTableQuery() {
        if (databaseManager.getType() == DatabaseType.MYSQL) {
            return "CREATE TABLE IF NOT EXISTS `banks` (" +
                    "`id` VARCHAR(10) NOT NULL PRIMARY KEY," +
                    "`ownerUuid` VARCHAR(36) NOT NULL UNIQUE," +
                    "`data` TEXT NOT NULL," +
                    "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "INDEX idx_owner_uuid (ownerUuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        } else {
            return "CREATE TABLE IF NOT EXISTS `banks` (" +
                    "`id` TEXT NOT NULL PRIMARY KEY," +
                    "`ownerUuid` TEXT NOT NULL UNIQUE," +
                    "`data` TEXT NOT NULL," +
                    "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";
        }
    }

    public void createBankAccount(BankAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Bank account cannot be null");
        }

        if (account.getBankId() == null || account.getOwnerUuid() == null) {
            throw new IllegalArgumentException("Bank ID and Owner UUID are required");
        }

        lock.writeLock().lock();
        try {
            // Prüfen ob Account bereits existiert
            if (hasBankAccount(account.getOwnerUuid())) {
                throw new IllegalStateException("Bank account already exists for owner: " + account.getOwnerUuid());
            }

            String query = "INSERT INTO `banks` (`id`, `ownerUuid`, `data`) VALUES (?, ?, ?)";
            Object[] parameters = {
                    account.getBankId(),
                    account.getOwnerUuid().toString(),
                    gson.toJson(account)
            };

            int rowsAffected = databaseManager.executeUpdate(query, parameters);
            if (rowsAffected <= 0) {
                throw new SQLException("Failed to create bank account");
            }

            LOGGER.info("Created bank account: " + account.getBankId() + " for owner: " + account.getOwnerUuid());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create bank account for owner: " + account.getOwnerUuid(), e);
            throw new RuntimeException("Failed to create bank account", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean hasBankAccount(UUID ownerUuid) {
        if (ownerUuid == null) {
            return false;
        }

        lock.readLock().lock();
        try {
            String query = "SELECT COUNT(*) FROM `banks` WHERE `ownerUuid` = ?";
            Object[] parameters = {ownerUuid.toString()};

            return databaseManager.executeQuery(query, parameters, resultSet -> {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            });
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking if bank account exists for owner: " + ownerUuid, e);
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public BankAccount getBankAccount(String bankId) {
        if (bankId == null || bankId.trim().isEmpty()) {
            return null;
        }

        lock.readLock().lock();
        try {
            String query = "SELECT `data` FROM `banks` WHERE `id` = ?";
            Object[] parameters = {bankId};

            return databaseManager.executeQuery(query, parameters, resultSet -> {
                if (resultSet.next()) {
                    String jsonData = resultSet.getString("data");
                    if (jsonData != null && !jsonData.trim().isEmpty()) {
                        try {
                            return gson.fromJson(jsonData, BankAccount.class);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Failed to deserialize bank account data for ID: " + bankId, e);
                        }
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error retrieving bank account by ID: " + bankId, e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public BankAccount getBankAccount(UUID ownerUuid) {
        if (ownerUuid == null) {
            return null;
        }

        lock.readLock().lock();
        try {
            String query = "SELECT `data` FROM `banks` WHERE `ownerUuid` = ?";
            Object[] parameters = {ownerUuid.toString()};

            return databaseManager.executeQuery(query, parameters, resultSet -> {
                if (resultSet.next()) {
                    String jsonData = resultSet.getString("data");
                    if (jsonData != null && !jsonData.trim().isEmpty()) {
                        try {
                            return gson.fromJson(jsonData, BankAccount.class);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Failed to deserialize bank account data for owner: " + ownerUuid, e);
                        }
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error retrieving bank account by owner UUID: " + ownerUuid, e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateBankAccount(BankAccount account) {
        if (account == null || account.getBankId() == null) {
            throw new IllegalArgumentException("Bank account and bank ID cannot be null");
        }

        lock.writeLock().lock();
        try {
            String query = "UPDATE `banks` SET `data` = ? WHERE `id` = ?";
            Object[] parameters = {
                    gson.toJson(account),
                    account.getBankId()
            };

            int rowsAffected = databaseManager.executeUpdate(query, parameters);
            if (rowsAffected <= 0) {
                LOGGER.warning("No rows updated for bank account: " + account.getBankId());
            } else {
                LOGGER.fine("Updated bank account: " + account.getBankId());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update bank account: " + account.getBankId(), e);
            throw new RuntimeException("Failed to update bank account", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setNewPin(BankAccount account, int pin) {
        if (account == null) {
            throw new IllegalArgumentException("Bank account cannot be null");
        }

        // PIN Validierung
        if (pin < 1000 || pin > 9999) {
            throw new IllegalArgumentException("PIN must be a 4-digit number (1000-9999)");
        }

        lock.writeLock().lock();
        try {
            account.setPin(pin);
            updateBankAccount(account);
            LOGGER.info("PIN updated for bank account: " + account.getBankId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addBalance(BankAccount account, double amount) {
        modifyBalance(account, amount, true);
    }

    public void removeBalance(BankAccount account, double amount) {
        modifyBalance(account, amount, false);
    }

    private void modifyBalance(BankAccount account, double amount, boolean isAddition) {
        if (account == null) {
            throw new IllegalArgumentException("Bank account cannot be null");
        }

        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Amount must be a valid number");
        }

        lock.writeLock().lock();
        try {
            // Verwende BigDecimal für präzise Berechnungen
            BigDecimal currentBalance = BigDecimal.valueOf(account.getBalance());
            BigDecimal changeAmount = BigDecimal.valueOf(amount);

            BigDecimal newBalance;
            if (isAddition) {
                newBalance = currentBalance.add(changeAmount);
            } else {
                newBalance = currentBalance.subtract(changeAmount);

                // Prüfe auf negative Bilanz
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Insufficient funds. Current balance: " +
                            currentBalance.toPlainString() + ", attempted withdrawal: " + changeAmount.toPlainString());
                }
            }

            // Runde auf 2 Dezimalstellen
            double finalBalance = newBalance.setScale(2, RoundingMode.HALF_UP).doubleValue();
            account.setBalance(finalBalance);
            updateBankAccount(account);

            LOGGER.info("Balance " + (isAddition ? "increased" : "decreased") + " by " + amount +
                    " for account: " + account.getBankId() + ", new balance: " + finalBalance);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addTransaction(BankAccount account, TransactionType type, double amount, UUID player) {
        if (account == null) {
            throw new IllegalArgumentException("Bank account cannot be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        if (player == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }

        lock.writeLock().lock();
        try {
            Transaction transaction = new Transaction(type, amount, System.currentTimeMillis(), player);
            account.getTransactions().add(transaction);

            // Limitiere auf konfigurierte Anzahl Transaktionen
            int maxTransactions = configManager.getMaxTransactionsPerAccount();
            while (account.getTransactions().size() > maxTransactions) {
                account.getTransactions().removeFirst();
            }

            updateBankAccount(account);
            LOGGER.fine("Added transaction to account " + account.getBankId() + ": " + type + " - " + amount);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setSuspended(BankAccount account, boolean suspended) {
        if (account == null) {
            throw new IllegalArgumentException("Bank account cannot be null");
        }

        lock.writeLock().lock();
        try {
            account.setSuspended(suspended);
            updateBankAccount(account);
            LOGGER.info("Account " + account.getBankId() + " suspension status changed to: " + suspended);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean deleteBankAccount(String bankId) {
        if (bankId == null || bankId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank ID cannot be null or empty");
        }

        lock.writeLock().lock();
        try {
            String query = "DELETE FROM `banks` WHERE `id` = ?";
            Object[] parameters = {bankId};

            int rowsAffected = databaseManager.executeUpdate(query, parameters);
            if (rowsAffected > 0) {
                LOGGER.info("Deleted bank account: " + bankId);
                return true;
            } else {
                LOGGER.warning("No bank account found to delete with ID: " + bankId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete bank account: " + bankId, e);
            throw new RuntimeException("Failed to delete bank account", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean transferFunds(BankAccount fromAccount, BankAccount toAccount, double amount) {
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("Both accounts cannot be null");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (fromAccount.getBankId().equals(toAccount.getBankId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        lock.writeLock().lock();
        try {
            // Prüfe verfügbares Guthaben
            if (fromAccount.getBalance() < amount) {
                LOGGER.warning("Transfer failed: insufficient funds. Account: " + fromAccount.getBankId() +
                        ", Balance: " + fromAccount.getBalance() + ", Transfer amount: " + amount);
                return false;
            }

            // Durchführung der Übertragung
            removeBalance(fromAccount, amount);
            addBalance(toAccount, amount);

            // Transaktionen hinzufügen
            addTransaction(fromAccount, TransactionType.TRANSFER_OUT, amount, toAccount.getOwnerUuid());
            addTransaction(toAccount, TransactionType.TRANSFER_IN, amount, fromAccount.getOwnerUuid());

            LOGGER.info("Successful transfer of " + amount + " from " + fromAccount.getBankId() +
                    " to " + toAccount.getBankId());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Transfer failed between accounts " + fromAccount.getBankId() +
                    " and " + toAccount.getBankId(), e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Neue Utility-Methoden mit Config-Integration

    public int getBankAccountCost() {
        return configManager.getBankCost();
    }

    public int getCardCost() {
        return configManager.getCardCost();
    }

    public String getCurrencySymbol() {
        return configManager.getSymbol();
    }

    public double getMaxBalance() {
        return configManager.getMaxBalance();
    }

    public int getMaxTransactionsPerAccount() {
        return configManager.getMaxTransactionsPerAccount();
    }

    public boolean isValidPin(int pin) {
        return pin >= configManager.getMinPin() && pin <= configManager.getMaxPin();
    }

    public boolean canAddBalance(BankAccount account, double amount) {
        if (account == null || amount <= 0) {
            return false;
        }

        double newBalance = account.getBalance() + amount;
        return newBalance <= configManager.getMaxBalance();
    }

    public boolean isAccountOperationAllowed(BankAccount account) {
        return account != null && !account.isSuspended();
    }

    public String formatBalance(double balance) {
        return String.format("%.2f%s", balance, getCurrencySymbol());
    }
}