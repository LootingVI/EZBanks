package de.flori.ezbanks.gui;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.enums.TransactionType;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.ItemBuilder;
import de.flori.ezbanks.utils.ItemUtils;
import de.flori.ezbanks.utils.MessageUtils;
import de.flori.ezbanks.utils.Utils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
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

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@NoArgsConstructor
public class BankAccountGUI implements InventoryHolder, Listener {

    private BankAccount account;

    public BankAccountGUI(BankAccount account) {
        this.account = account;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (account == null) throw new IllegalStateException("BankAccount is null!");

        final Inventory inventory = Bukkit.createInventory(this, 45, Component.text("§6Bank"));

        final ItemStack frameItemStack = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<reset>"))
                .addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                .build();

        for (int i = 0; i < 9; i++) inventory.setItem(i, frameItemStack);

        inventory.setItem(9, frameItemStack);
        inventory.setItem(17, frameItemStack);
        inventory.setItem(18, frameItemStack);
        inventory.setItem(26, frameItemStack);
        inventory.setItem(27, frameItemStack);
        inventory.setItem(35, frameItemStack);

        for (int i = 36; i < 45; i++) inventory.setItem(i, frameItemStack);

        final ItemStack transferItemStack = new ItemBuilder(Material.ARROW)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Send Money").decoration(TextDecoration.ITALIC, false))
                .setLore(MiniMessage.miniMessage().deserialize("<grey>Send Money to another bank account!").decoration(TextDecoration.ITALIC, false))
                .build();

        final ItemStack infoItemStack = new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Owner: <gold>" + Bukkit.getOfflinePlayer(account.getOwnerUuid()).getName()).decoration(TextDecoration.ITALIC, false))
                .setLore(
                        MiniMessage.miniMessage().deserialize("<aqua>ID: <gold>" + account.getBankId()).decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<aqua>Balance: <gold>" + account.getBalance() + EZBanks.getInstance().getConfigManager().getSymbol()).decoration(TextDecoration.ITALIC, false)
                )
                .build();

        final ItemStack depositItemStack = new ItemBuilder(Material.ANVIL)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Deposit a custom amount of Money").decoration(TextDecoration.ITALIC, false))
                .setLore(MiniMessage.miniMessage().deserialize("<grey>Deposit a set amount of Money").decoration(TextDecoration.ITALIC, false))
                .build();

        final ItemStack withdrawItemStack = new ItemBuilder(Material.DISPENSER)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Withdraw a custom amount of Money").decoration(TextDecoration.ITALIC, false))
                .setLore(MiniMessage.miniMessage().deserialize("<grey>Withdraw a custom amount of money").decoration(TextDecoration.ITALIC, false))
                .build();

        final StringBuilder builder = new StringBuilder();
        account.getTransactions().reversed().forEach(transaction -> {
            builder.append(transaction.getType().getDisplayName()).append(" <gold>").append(transaction.getAmount()).append(EZBanks.getInstance().getConfigManager().getSymbol()).append("<aqua> ").append(Bukkit.getOfflinePlayer(transaction.getPlayer()).getName()).append(" <gray>").append(Utils.DATE_AND_TIME_FORMAT.format(transaction.getTimestamp())).append('\n');
        });

        final ItemStack accountStatementItemStack = new ItemBuilder(Material.PAPER)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<aqua>Bank Transactions").decoration(TextDecoration.ITALIC, false))
                .setLore(Arrays.stream(builder.toString().split("\n")).map(s -> MiniMessage.miniMessage().deserialize(s).decoration(TextDecoration.ITALIC, false)).toList())
                .build();

        inventory.setItem(10, transferItemStack);
        inventory.setItem(16, depositItemStack);
        inventory.setItem(22, infoItemStack);
        inventory.setItem(28, withdrawItemStack);
        inventory.setItem(34, accountStatementItemStack);

        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (!(event.getInventory().getHolder() instanceof BankAccountGUI)) return;
            event.setCancelled(true);

            final ItemStack item = event.getCurrentItem();
            if (item == null)
                return;

            final Player player = (Player) event.getWhoClicked();

            final ItemStack bankCardItemStack = player.getInventory().getItemInMainHand();
            if (!ItemUtils.isBankCard(bankCardItemStack)) {
                player.sendMessage(Component.text(EZBanks.getPrefix() + "§cNo bank card recognised! Please hold a bank card in your hand while using the bank menu."));
                player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 0.0f, 1.0f);
                return;
            }

            final BankAccount account = EZBanks.getInstance().getBankManager().getBankAccount(ItemUtils.getBankId(bankCardItemStack));
            switch (item.getType()) {
                case ARROW -> {
                    final SignGUI bankIdGui = SignGUI.builder()
                            .setLines(null, "-----------", "§cEnter the bank id", "-----------")
                            .setType(Material.ACACIA_SIGN)
                            .setColor(DyeColor.GRAY)
                            .setHandler((p, result) -> {
                                final String targetBankId = result.getLineWithoutColor(0);

                                if (targetBankId.isEmpty()) {
                                    p.sendMessage(Component.text(EZBanks.getPrefix() + "§cPlease enter a correct bank id!"));
                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                    return List.of();
                                }

                                final BankAccount targetAccount = EZBanks.getInstance().getBankManager().getBankAccount(targetBankId);

                                if (targetAccount == null) {
                                    p.sendMessage(Component.text(EZBanks.getPrefix() + "§cBank account not found!"));
                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                    return List.of();
                                }

                                if(Objects.equals(targetAccount.getBankId(), EZBanks.getInstance().getBankManager().getBankAccount(p.getUniqueId()).getBankId())){
                                    p.sendMessage(EZBanks.getPrefix() + "§cYou cant send money to your own account!");
                                    return List.of();
                                }

                                return List.of(SignGUIAction.run(() -> {
                                    final SignGUI amountGui = SignGUI.builder()
                                            .setLines(null, "-----------", "§cEnter amount to transfer", "-----------")
                                            .setType(Material.ACACIA_SIGN)
                                            .setColor(DyeColor.GRAY)
                                            .setHandler((p1, result1) -> {
                                                final String amount = result1.getLineWithoutColor(0);

                                                if (amount.isEmpty() || !MessageUtils.isValidInteger(amount)) {
                                                    p1.sendMessage(Component.text(EZBanks.getPrefix() + "§cPlease enter a correct amount!"));
                                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                                    return List.of();
                                                }

                                                final int amountInt = Integer.parseInt(amount);
                                                final double balance = account.getBalance();
                                                if(amountInt < 0.1){
                                                    p.sendMessage(Component.text(EZBanks.getPrefix() + "§cPlease enter a correct amount!"));
                                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                                }else{
                                                    if (amountInt > balance) {
                                                        p1.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou don't have enough money in your bank account!"));
                                                        player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                                        return List.of();
                                                    }

                                                    final Player targetPlayer = Bukkit.getPlayer(targetAccount.getOwnerUuid());
                                                    if (targetPlayer != null) {
                                                        targetPlayer.sendMessage(Component.text(EZBanks.getPrefix() + "§aYou have received a bank transfer from §b" + p1.getName() + "§a. Amount: §6" + amount + EZBanks.getInstance().getConfigManager().getSymbol()));
                                                    }

                                                    EZBanks.getInstance().getBankManager().removeBalance(account, amountInt);
                                                    EZBanks.getInstance().getBankManager().addBalance(targetAccount, amountInt);
                                                    EZBanks.getInstance().getBankManager().addTransaction(account, TransactionType.TRANSFER_OUT, amountInt, p.getUniqueId());
                                                    EZBanks.getInstance().getBankManager().addTransaction(targetAccount, TransactionType.TRANSFER_IN, amountInt, p.getUniqueId());
                                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                                                    p1.sendMessage(Component.text(EZBanks.getPrefix() + "§aYou have successfully transferred §6" + amount + EZBanks.getInstance().getConfigManager().getSymbol() + " §ato §b" + Bukkit.getOfflinePlayer(targetAccount.getOwnerUuid()).getName()));
                                                    return List.of();
                                                }
                                                return List.of();
                                            })
                                            .build();

                                    amountGui.open(player);
                                }));
                            })
                            .build();

                    bankIdGui.open(player);
                }
                case DISPENSER -> {
                    final SignGUI gui = SignGUI.builder()
                            .setLines(null, "-----------", "§cEnter amount to withdraw", "-----------")
                            .setType(Material.ACACIA_SIGN)
                            .setColor(DyeColor.GRAY)
                            .setHandler((p, result) -> {
                                final String amount = result.getLineWithoutColor(0);

                                if (amount.isEmpty() || !MessageUtils.isValidInteger(amount)) {
                                    p.sendMessage(Component.text(EZBanks.getPrefix() + "§cPlease enter a correct amount!"));
                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                    return List.of();
                                }

                                final int amountInt = Integer.parseInt(amount);
                                final double balance = account.getBalance();

                                if(amountInt < 0.1){
                                   p.sendMessage(Component.text(EZBanks.getPrefix() + "§cPlease enter a correct amount!"));
                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                }else{
                                    if (amountInt > balance) {
                                        p.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou don't have enough money in your bank account!"));
                                        player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                        return List.of();
                                    }

                                    EZBanks.getInstance().getBankManager().removeBalance(account, amountInt);
                                    EZBanks.getInstance().getEconomy().depositPlayer(p, amountInt);
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                                    EZBanks.getInstance().getBankManager().addTransaction(account, TransactionType.REMOVE_MONEY, amountInt, p.getUniqueId());

                                    p.sendMessage(Component.text(EZBanks.getPrefix() + "§aYou have successfully withdrawn §6" + amount + EZBanks.getInstance().getConfigManager().getSymbol()));
                                    return List.of();
                                }
                                return List.of();
                            })
                            .build();

                    gui.open(player);
                }
                case ANVIL -> {
                    final SignGUI gui = SignGUI.builder()
                            .setLines(null, "-----------", "§cEnter amount to deposit", "-----------")
                            .setType(Material.ACACIA_SIGN)
                            .setColor(DyeColor.GRAY)
                            .setHandler((p, result) -> {
                                final String amount = result.getLineWithoutColor(0);

                                if (amount.isEmpty() || !MessageUtils.isValidInteger(amount)) {
                                    p.sendMessage(Component.text(EZBanks.getPrefix() + "§cPlease enter a correct amount!"));
                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                    return List.of();
                                }

                                final int amountInt = Integer.parseInt(amount);
                                final double balance = EZBanks.getInstance().getEconomy().getBalance(p);

                                if(amountInt < 0.1){
                                    p.sendMessage(Component.text(EZBanks.getPrefix() + "§cPlease enter a correct amount!"));
                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                }else{
                                    if (amountInt > balance) {
                                        p.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou don't have enough money in your inventory!"));
                                        player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                        return List.of();
                                    }

                                    EZBanks.getInstance().getBankManager().addBalance(account, amountInt);
                                    EZBanks.getInstance().getEconomy().withdrawPlayer(p, amountInt);

                                    EZBanks.getInstance().getBankManager().addTransaction(account, TransactionType.ADD_MONEY, amountInt, p.getUniqueId());
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                                    p.sendMessage(Component.text(EZBanks.getPrefix() + "§aYou have successfully deposited §6" + amount + EZBanks.getInstance().getConfigManager().getSymbol()));
                                    return List.of();
                                }
                                return List.of();
                            })
                            .build();

                    gui.open(player);
                }
            }
        } catch (NullPointerException | NoSuchElementException ignored) {}
    }

}
