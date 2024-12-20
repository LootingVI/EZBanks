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

public class BuyCardGUI implements InventoryHolder, Listener {

    private final Inventory inventory;

    public BuyCardGUI() {
        this.inventory = Bukkit.createInventory(this, 45, Component.text("§cNew card"));
    }

    @Override
    public @NotNull Inventory getInventory() {
        final ItemStack itemStack = new ItemBuilder(Material.BOOK)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<yellow>Buy a new credit card").decoration(TextDecoration.ITALIC, false))
                .setLore(MiniMessage.miniMessage().deserialize("<red>Cost: <gold>" + EZBanks.getInstance().getConfigManager().getCardCost() + EZBanks.getInstance().getConfigManager().getSymbol()).decoration(TextDecoration.ITALIC, false))
                .build();

        this.inventory.setItem(22, itemStack);
        return this.inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (!(event.getInventory().getHolder() instanceof BuyCardGUI)) return;
            event.setCancelled(true);

            final ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() != Material.BOOK)
                return;

            final Player player = (Player) event.getWhoClicked();

            if (!EZBanks.getInstance().getBankManager().hasBankAccount(player.getUniqueId())) {
                player.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou do not have a bank account."));
                player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                return;
            }

            final int cardCost = EZBanks.getInstance().getConfigManager().getCardCost();
            final double balance = EZBanks.getInstance().getEconomy().getBalance(player);

            if (balance < cardCost) {
                player.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou don't have enough money to buy a new card."));
                player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                return;
            }

            EZBanks.getInstance().getEconomy().withdrawPlayer(player, cardCost);

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            final BankAccount bankAccount = EZBanks.getInstance().getBankManager().getBankAccount(player.getUniqueId());
            player.getInventory().addItem(ItemUtils.getBankCard(bankAccount));

            player.sendMessage(Component.text(EZBanks.getPrefix() + "§aYou have successfully bought a new card."));
            player.sendMessage(Component.text(EZBanks.getPrefix() + "§cI hope you remember your PIN? If not run /setpin"));
        } catch (NullPointerException ignored) {}
    }

}
