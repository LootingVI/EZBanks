package de.flori.ezbanks.gui;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.ItemBuilder;
import de.flori.ezbanks.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BuybankAccountGUI implements InventoryHolder, Listener {

    private final Inventory inventory;

    public BuybankAccountGUI() {
        this.inventory = Bukkit.createInventory(this, 45, Component.text("§cNew account"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        final ItemStack itemStack = new ItemBuilder(Material.BOOK)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<yellow>Buy a bank account").decoration(TextDecoration.ITALIC, false))
                .setLore(MiniMessage.miniMessage().deserialize("<red>Cost: <gold>" + EZBanks.getInstance().getConfigManager().getBankCost() + EZBanks.getInstance().getConfigManager().getSymbol()).decoration(TextDecoration.ITALIC, false))
                .build();

        this.inventory.setItem(22, itemStack);
        return this.inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (!(event.getInventory().getHolder() instanceof BuybankAccountGUI)) return;
            event.setCancelled(true);

            final ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() != Material.BOOK)
                return;

            final Player player = (Player) event.getWhoClicked();

            final int cardCost = EZBanks.getInstance().getConfigManager().getCardCost();
            final double balance = EZBanks.getInstance().getEconomy().getBalance(player);
            final int accost = EZBanks.getInstance().getConfigManager().getBankCost();

            if (balance < accost) {
                player.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou don't have enough money to buy a bank account."));
                player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                return;
            }

            EZBanks.getInstance().getEconomy().withdrawPlayer(player, accost);

            final String bankId = UUID.randomUUID().toString().split("-")[0];
            final int pin = ThreadLocalRandom.current().nextInt(1000, 10000);

            final BankAccount account = new BankAccount();
            account.setBankId(bankId);
            account.setOwnerUuid(player.getUniqueId());
            account.setPin(pin);

            EZBanks.getInstance().getBankManager().createBankAccount(account);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            player.sendMessage(Component.text(EZBanks.getPrefix() + "§aYou have successfully created a new account. Your bank account pin is: " + pin));
            player.sendMessage(Component.text("§cBut remember them well! You can't access your bank account without it!"));
            player.getInventory().addItem(ItemUtils.getBankCard(account));
            player.getInventory().close();
        } catch (NullPointerException ignored) {}
    }

}
