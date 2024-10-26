package de.flori.ezbanks.gui;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.ItemBuilder;
import de.flori.ezbanks.utils.ItemUtils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BankMenuGUI implements InventoryHolder, Listener {

    @Override
    public @NotNull Inventory getInventory() {
        final Inventory inventory = Bukkit.createInventory(this, 27, Component.text("§cBank menu"));

        final ItemStack openBankItemStack = new ItemBuilder(Material.CHEST)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<yellow>Open bank menu").decoration(TextDecoration.ITALIC, false))
                .setLore(MiniMessage.miniMessage().deserialize("<green>Open the main bank menu").decoration(TextDecoration.ITALIC, false))
                .build();

        final ItemStack resetPinItemStack = new ItemBuilder(Material.NAME_TAG)
                .setDisplayName(MiniMessage.miniMessage().deserialize("<yellow>Reset card PIN").decoration(TextDecoration.ITALIC, false))
                .setLore(MiniMessage.miniMessage().deserialize("<green>Change ur bank card pin").decoration(TextDecoration.ITALIC, false))
                .build();

        inventory.setItem(11, openBankItemStack);
        inventory.setItem(15, resetPinItemStack);

        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (!(event.getInventory().getHolder() instanceof BankMenuGUI)) return;
            event.setCancelled(true);

            final ItemStack item = event.getCurrentItem();
            if (item == null)
                return;

            final Player player = (Player) event.getWhoClicked();

            switch (item.getType()) {
                case CHEST: {
                    final ItemStack itemStack = player.getInventory().getItemInMainHand();
                    if (!ItemUtils.isBankCard(itemStack)) {
                        player.sendMessage(Component.text(EZBanks.getPrefix() + "§cNo bank card recognised! Please hold a bank card in your hand."));
                        player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                        return;
                    }

                    final String bankId = ItemUtils.getBankId(itemStack);
                    final BankAccount account = EZBanks.getInstance().getBankManager().getBankAccount(bankId);

                    if (account == null) {
                        player.sendMessage(Component.text(EZBanks.getPrefix() + "§cThis bank account does not exist anymore."));
                        player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                        return;
                    }

                    final SignGUI signGUI = SignGUI.builder()
                            .setLines(null, "§7-----------", "§cPlease enter your PIN", "§7-----------")
                            .setType(Material.ACACIA_SIGN)
                            .setColor(DyeColor.GRAY)
                            .setHandler((p, result) -> {
                                final String input = result.getLineWithoutColor(0);
                                final int pin = account.getPin();

                                if (!input.equals(String.valueOf(pin))) {
                                    p.sendMessage(EZBanks.getPrefix() + "§cThe PIN you entered is incorrect.");
                                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                    return List.of();
                                }
                                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                                return List.of(SignGUIAction.openInventory(EZBanks.getInstance(), new BankAccountGUI(account).getInventory()));
                            })
                            .build();

                    signGUI.open(player);
                    break;
                }
                case NAME_TAG: {
                    player.closeInventory();
                    player.performCommand("setpin");
                    break;
                }
            }
        } catch (NullPointerException ignored) {}
    }

}
