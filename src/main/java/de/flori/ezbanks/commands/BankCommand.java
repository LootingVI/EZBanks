package de.flori.ezbanks.commands;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.functions.CreateBankAccount;
import de.flori.ezbanks.utils.ItemBuilder;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.bukkit.persistence.PersistentDataType.*;

public class BankCommand implements BasicCommand {

    public Inventory inv = null;
    public Inventory inv1 = null;

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] strings) {

        Player player = (Player) commandSourceStack.getSender();
        String prefix = EZBanks.getInstance().configManager().getPrefix();
        String symbol = EZBanks.getInstance().configManager().getSymbol();
        int cardcost = EZBanks.getInstance().configManager().getCardCost();


        if (EZBanks.getInstance().bankManager().getBankAccount(player.getUniqueId()) == null) {
            new CreateBankAccount(player.getUniqueId());
        } else {

            if(!player.getInventory().contains(Material.PAPER)){
                this.inv1 = player.getServer().createInventory(null, 45, "§cNew card");

                ItemStack buynewcard = new ItemBuilder(Material.BOOK)
                        .setDisplayName("§eBuy a new credit card")
                        .setLore("§cCost: §6" + cardcost + symbol)
                        .build();

                this.inv1.setItem(22, buynewcard);

                player.openInventory(inv1);
            }

            if(player.getItemInHand().getType() == Material.PAPER){

                NamespacedKey key = new NamespacedKey(EZBanks.getInstance(), "bankid");
                PersistentDataContainer container = player.getItemInHand().getItemMeta().getPersistentDataContainer();
                if (container.has(key, PersistentDataType.STRING)) {
                    this.inv = player.getServer().createInventory(null, 27, "§cBank menu");

                    ItemStack openBankMenu = new ItemBuilder(Material.CHEST)
                            .setDisplayName("§eOpen bank menu")
                            .setLore("§aOpen the main bank menu")
                            .build();

                    ItemStack resetPin = new ItemBuilder(Material.NAME_TAG)
                            .setDisplayName("§eReset card PIN")
                            .setLore("§aChange ur bank card pin")
                            .build();

                    this.inv.setItem(11, openBankMenu);
                    this.inv.setItem(15, resetPin);

                    player.openInventory(inv);
                } else {
                    player.sendMessage(prefix + "§cNo bank card recognised! Please hold a bank card in your hand.");
                }
            } else {
                player.sendMessage(prefix + "§cNo bank card recognised! Please hold a bank card in your hand.");
            }

        }
    }
}
