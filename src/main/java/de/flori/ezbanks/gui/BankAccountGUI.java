package de.flori.ezbanks.gui;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.BankManager;
import de.flori.ezbanks.manager.enums.TransactionType;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.ItemBuilder;
import de.flori.ezbanks.utils.ItemUtils;
import de.flori.ezbanks.utils.MessageUtils;
import de.flori.ezbanks.utils.Utils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@NoArgsConstructor
public class BankAccountGUI implements InventoryHolder, Listener {

    private static final Logger LOGGER = Logger.getLogger(BankAccountGUI.class.getName());
    private static final double MIN_TRANSACTION_AMOUNT = 0.01;

    private BankAccount account;

    public BankAccountGUI(BankAccount account) {
        this.account = account;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (account == null) {
            throw new IllegalStateException("BankAccount cannot be null!");
        }

        final Inventory inventory = Bukkit.createInventory(this, 45,
                Component.text("§6Bank - " + account.getBankId()));

        setupInventoryFrame(inventory);
        setupInventoryItems(inventory);

        return inventory;
    }

    private void setupInventoryFrame(Inventory inventory) {
        final ItemStack frameItemStack = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<reset>"))
                .addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                .build();

        // Top border
        for (int i = 0; i < 9; i++) inventory.setItem(i, frameItemStack);

        // Side borders
        inventory.setItem(9, frameItemStack);
        inventory.setItem(17, frameItemStack);
        inventory.setItem(18, frameItemStack);
        inventory.setItem(26, frameItemStack);
        inventory.setItem(27, frameItemStack);
        inventory.setItem(35, frameItemStack);

        // Bottom border
        for (int i = 36; i < 45; i++) inventory.setItem(i, frameItemStack);
    }

    private void setupInventoryItems(Inventory inventory) {
        BankManager bankManager = EZBanks.getInstance().getBankManager();
        String currencySymbol = bankManager.getCurrencySymbol();

        // Transfer Money Item
        final ItemStack transferItemStack = new ItemBuilder(Material.ARROW)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Send Money").decoration(TextDecoration.ITALIC, false))
                .setLore(
                        MiniMessage.miniMessage().deserialize("<grey>Send money to another bank account").decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<grey>Minimum amount: <yellow>" + MIN_TRANSACTION_AMOUNT + currencySymbol).decoration(TextDecoration.ITALIC, false)
                )
                .build();

        // Account Info Item
        String ownerName = Bukkit.getOfflinePlayer(account.getOwnerUuid()).getName();
        if (ownerName == null) ownerName = "Unknown";

        final ItemStack infoItemStack = new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Account Information").decoration(TextDecoration.ITALIC, false))
                .setLore(
                        MiniMessage.miniMessage().deserialize("<aqua>Owner: <gold>" + ownerName).decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<aqua>ID: <gold>" + account.getBankId()).decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<aqua>Balance: <gold>" + bankManager.formatBalance(account.getBalance())).decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<aqua>Status: " + (account.isSuspended() ? "<red>Suspended" : "<green>Active")).decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<grey>Max Balance: <yellow>" + bankManager.formatBalance(bankManager.getMaxBalance())).decoration(TextDecoration.ITALIC, false)
                )
                .build();

        // Deposit Item
        final ItemStack depositItemStack = new ItemBuilder(Material.ANVIL)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Deposit Money").decoration(TextDecoration.ITALIC, false))
                .setLore(
                        MiniMessage.miniMessage().deserialize("<grey>Deposit money from your wallet").decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<grey>Minimum amount: <yellow>" + MIN_TRANSACTION_AMOUNT + currencySymbol).decoration(TextDecoration.ITALIC, false)
                )
                .build();

        // Withdraw Item
        final ItemStack withdrawItemStack = new ItemBuilder(Material.DISPENSER)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Withdraw Money").decoration(TextDecoration.ITALIC, false))
                .setLore(
                        MiniMessage.miniMessage().deserialize("<grey>Withdraw money to your wallet").decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<grey>Minimum amount: <yellow>" + MIN_TRANSACTION_AMOUNT + currencySymbol).decoration(TextDecoration.ITALIC, false)
                )
                .build();

        // Transaction History Item
        final ItemStack accountStatementItemStack = createTransactionHistoryItem(currencySymbol);

        // Set items in inventory
        inventory.setItem(10, transferItemStack);
        inventory.setItem(16, depositItemStack);
        inventory.setItem(22, infoItemStack);
        inventory.setItem(28, withdrawItemStack);
        inventory.setItem(34, accountStatementItemStack);
    }

    private ItemStack createTransactionHistoryItem(String currencySymbol) {
        final StringBuilder builder = new StringBuilder();

        if (account.getTransactions().isEmpty()) {
            builder.append("<grey>No transactions yet");
        } else {
            account.getTransactions().reversed().forEach(transaction -> {
                String playerName = Bukkit.getOfflinePlayer(transaction.getPlayer()).getName();
                if (playerName == null) playerName = "Unknown";

                builder.append(transaction.getType().getDisplayName())
                        .append(" <gold>").append(String.format("%.2f", transaction.getAmount()))
                        .append(currencySymbol).append("<aqua> ")
                        .append(playerName).append(" <gray>")
                        .append(Utils.DATE_AND_TIME_FORMAT.format(transaction.getTimestamp()))
                        .append('\n');
            });
        }

        return new ItemBuilder(Material.PAPER)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Transaction History").decoration(TextDecoration.ITALIC, false))
                .setLore(Arrays.stream(builder.toString().split("\n"))
                        .map(s -> MiniMessage.miniMessage().deserialize(s).decoration(TextDecoration.ITALIC, false))
                        .toList())
                .build();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (!(event.getInventory().getHolder() instanceof BankAccountGUI)) return;
            event.setCancelled(true);

            final ItemStack item = event.getCurrentItem();
            if (item == null) return;

            final Player player = (Player) event.getWhoClicked();

            // Validate bank card
            if (!validateBankCard(player)) {
                return;
            }

            // Get current account data
            final ItemStack bankCardItemStack = player.getInventory().getItemInMainHand();
            final BankAccount currentAccount = EZBanks.getInstance().getBankManager()
                    .getBankAccount(ItemUtils.getBankId(bankCardItemStack));

            if (currentAccount == null) {
                sendErrorMessage(player, "Bank account not found!");
                return;
            }

            if (currentAccount.isSuspended()) {
                sendErrorMessage(player, "Your bank account is suspended!");
                return;
            }

            // Handle different actions
            switch (item.getType()) {
                case ARROW -> handleTransfer(player, currentAccount);
                case DISPENSER -> handleWithdraw(player, currentAccount);
                case ANVIL -> handleDeposit(player, currentAccount);
                default -> {}
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling bank account GUI click", e);
            sendErrorMessage((Player) event.getWhoClicked(), "An error occurred while processing your request.");
        }
    }

    private boolean validateBankCard(Player player) {
        final ItemStack bankCardItemStack = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isBankCard(bankCardItemStack)) {
            sendErrorMessage(player, "No bank card recognized! Please hold a bank card in your hand.");
            return false;
        }
        return true;
    }

    private void handleTransfer(Player player, BankAccount account) {
        try {
            final SignGUI bankIdGui = SignGUI.builder()
                    .setLines(null, "-----------", "§cEnter bank ID", "-----------")
                    .setType(Material.ACACIA_SIGN)
                    .setColor(DyeColor.GRAY)
                    .setHandler((p, result) -> {
                        final String targetBankId = result.getLineWithoutColor(0);

                        if (targetBankId == null || targetBankId.trim().isEmpty()) {
                            sendErrorMessage(p, "Please enter a valid bank ID!");
                            return List.of();
                        }

                        final BankAccount targetAccount = EZBanks.getInstance().getBankManager()
                                .getBankAccount(targetBankId.trim());

                        if (targetAccount == null) {
                            sendErrorMessage(p, "Bank account not found!");
                            return List.of();
                        }

                        if (Objects.equals(targetAccount.getBankId(), account.getBankId())) {
                            sendErrorMessage(p, "You cannot send money to your own account!");
                            return List.of();
                        }

                        if (targetAccount.isSuspended()) {
                            sendErrorMessage(p, "The target account is suspended!");
                            return List.of();
                        }

                        return List.of(SignGUIAction.run(() -> openTransferAmountGui(player, account, targetAccount)));
                    })
                    .build();

            bankIdGui.open(player);
        } catch (SignGUIVersionException e) {
            LOGGER.log(Level.SEVERE, "Error creating transfer GUI", e);
            sendErrorMessage(player, "Failed to open transfer interface.");
        }
    }

    private void openTransferAmountGui(Player player, BankAccount fromAccount, BankAccount toAccount) {
        try {
            final SignGUI amountGui = SignGUI.builder()
                    .setLines(null, "-----------", "§cEnter amount", "-----------")
                    .setType(Material.ACACIA_SIGN)
                    .setColor(DyeColor.GRAY)
                    .setHandler((p, result) -> {
                        final String amountStr = result.getLineWithoutColor(0);

                        if (!isValidAmount(amountStr)) {
                            sendErrorMessage(p, "Please enter a valid amount!");
                            return List.of();
                        }

                        final double amount = parseAmount(amountStr);

                        if (amount < MIN_TRANSACTION_AMOUNT) {
                            sendErrorMessage(p, "Minimum transfer amount is " +
                                    EZBanks.getInstance().getBankManager().formatBalance(MIN_TRANSACTION_AMOUNT));
                            return List.of();
                        }

                        return processTransfer(p, fromAccount, toAccount, amount);
                    })
                    .build();

            amountGui.open(player);
        } catch (SignGUIVersionException e) {
            LOGGER.log(Level.SEVERE, "Error creating amount GUI", e);
            sendErrorMessage(player, "Failed to open amount interface.");
        }
    }

    private List<SignGUIAction> processTransfer(Player player, BankAccount fromAccount, BankAccount toAccount, double amount) {
        try {
            BankManager bankManager = EZBanks.getInstance().getBankManager();

            if (!bankManager.transferFunds(fromAccount, toAccount, amount)) {
                return List.of(); // Error messages are handled in transferFunds
            }

            // Notify recipient if online
            final Player targetPlayer = Bukkit.getPlayer(toAccount.getOwnerUuid());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(Component.text(EZBanks.getPrefix() +
                        "§aYou received a transfer from §b" + player.getName() +
                        "§a. Amount: §6" + bankManager.formatBalance(amount)));
            }

            sendSuccessMessage(player, "Successfully transferred " + bankManager.formatBalance(amount) +
                    " to " + Bukkit.getOfflinePlayer(toAccount.getOwnerUuid()).getName());

            return List.of();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing transfer", e);
            sendErrorMessage(player, "Transfer failed due to an error.");
            return List.of();
        }
    }

    private void handleWithdraw(Player player, BankAccount account) {
        try {
            final SignGUI gui = SignGUI.builder()
                    .setLines(null, "-----------", "§cEnter amount", "-----------")
                    .setType(Material.ACACIA_SIGN)
                    .setColor(DyeColor.GRAY)
                    .setHandler((p, result) -> {
                        final String amountStr = result.getLineWithoutColor(0);

                        if (!isValidAmount(amountStr)) {
                            sendErrorMessage(p, "Please enter a valid amount!");
                            return List.of();
                        }

                        final double amount = parseAmount(amountStr);

                        if (amount < MIN_TRANSACTION_AMOUNT) {
                            sendErrorMessage(p, "Minimum withdrawal amount is " +
                                    EZBanks.getInstance().getBankManager().formatBalance(MIN_TRANSACTION_AMOUNT));
                            return List.of();
                        }

                        if (amount > account.getBalance()) {
                            sendErrorMessage(p, "Insufficient funds in your bank account!");
                            return List.of();
                        }

                        try {
                            BankManager bankManager = EZBanks.getInstance().getBankManager();
                            bankManager.removeBalance(account, amount);
                            EZBanks.getInstance().getEconomy().depositPlayer(p, amount);
                            bankManager.addTransaction(account, TransactionType.REMOVE_MONEY, amount, p.getUniqueId());

                            sendSuccessMessage(p, "Successfully withdrew " + bankManager.formatBalance(amount));
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error processing withdrawal", e);
                            sendErrorMessage(p, "Withdrawal failed due to an error.");
                        }

                        return List.of();
                    })
                    .build();

            gui.open(player);
        } catch (SignGUIVersionException e) {
            LOGGER.log(Level.SEVERE, "Error creating withdrawal GUI", e);
            sendErrorMessage(player, "Failed to open withdrawal interface.");
        }
    }

    private void handleDeposit(Player player, BankAccount account) {
        try {
            final SignGUI gui = SignGUI.builder()
                    .setLines(null, "-----------", "§cEnter amount", "-----------")
                    .setType(Material.ACACIA_SIGN)
                    .setColor(DyeColor.GRAY)
                    .setHandler((p, result) -> {
                        final String amountStr = result.getLineWithoutColor(0);

                        if (!isValidAmount(amountStr)) {
                            sendErrorMessage(p, "Please enter a valid amount!");
                            return List.of();
                        }

                        final double amount = parseAmount(amountStr);

                        if (amount < MIN_TRANSACTION_AMOUNT) {
                            sendErrorMessage(p, "Minimum deposit amount is " +
                                    EZBanks.getInstance().getBankManager().formatBalance(MIN_TRANSACTION_AMOUNT));
                            return List.of();
                        }

                        final double walletBalance = EZBanks.getInstance().getEconomy().getBalance(p);
                        if (amount > walletBalance) {
                            sendErrorMessage(p, "Insufficient funds in your wallet!");
                            return List.of();
                        }

                        BankManager bankManager = EZBanks.getInstance().getBankManager();
                        if (!bankManager.canAddBalance(account, amount)) {
                            sendErrorMessage(p, "Deposit would exceed maximum balance limit!");
                            return List.of();
                        }

                        try {
                            bankManager.addBalance(account, amount);
                            EZBanks.getInstance().getEconomy().withdrawPlayer(p, amount);
                            bankManager.addTransaction(account, TransactionType.ADD_MONEY, amount, p.getUniqueId());

                            sendSuccessMessage(p, "Successfully deposited " + bankManager.formatBalance(amount));
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error processing deposit", e);
                            sendErrorMessage(p, "Deposit failed due to an error.");
                        }

                        return List.of();
                    })
                    .build();

            gui.open(player);
        } catch (SignGUIVersionException e) {
            LOGGER.log(Level.SEVERE, "Error creating deposit GUI", e);
            sendErrorMessage(player, "Failed to open deposit interface.");
        }
    }

    private boolean isValidAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr.trim());
            return amount > 0 && !Double.isNaN(amount) && !Double.isInfinite(amount);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double parseAmount(String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr.trim());
            return amount.setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void sendErrorMessage(Player player, String message) {
        player.sendMessage(Component.text(EZBanks.getPrefix() + "§c" + message));
        player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
    }

    private void sendSuccessMessage(Player player, String message) {
        player.sendMessage(Component.text(EZBanks.getPrefix() + "§a" + message));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }
}