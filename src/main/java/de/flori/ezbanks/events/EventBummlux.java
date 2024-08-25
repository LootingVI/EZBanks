package de.flori.ezbanks.events;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.utils.ItemBuilder;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.bukkit.persistence.PersistentDataType.STRING;

public class EventBummlux implements Listener {

    public Inventory inv = null;

    @EventHandler
    public void onFick(InventoryClickEvent event){


        Player player = (Player) event.getWhoClicked();

        NamespacedKey key = new NamespacedKey(EZBanks.getInstance(), "bankid");
       //  PersistentDataContainer container = player.getItemInHand().getItemMeta().getPersistentDataContainer();
       // String bankId = container.get(key, STRING);
       // double bal = EZBanks.getInstance().bankManager().getBankAccount(bankId).getBalance();
        String bankid1 = EZBanks.getInstance().bankManager().getBankAccount(player.getUniqueId()).getBankId();
        String symbol = EZBanks.getInstance().configManager().getSymbol();
        double balance = EZBanks.getEconomy().getBalance(player);
        int cardcost = EZBanks.getInstance().configManager().getCardCost();
//skibidi

        String prefix = EZBanks.getInstance().configManager().getPrefix();

        if(event.getCurrentItem() == null)return;

        if (event.getView().getTitle().equals("§cNew card")) {

            if(event.getCurrentItem().getType() == Material.BOOK){
                if(balance >= cardcost){
                    EZBanks.getEconomy().withdrawPlayer(player, cardcost);

                    ItemStack ex = new ItemBuilder(Material.PAPER)
                            .setDisplayName("§6Bank Card&7("+ bankid1 +")")
                            .setLore("§r§7Bank Owner: " + player.getName())
                            .setPersistentDataContainer("bankid", bankid1)
                            .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                            .build();

                    player.getInventory().addItem(ex);

                    player.sendMessage(prefix + "§aYou have successfully bought a new bank card!");
                    player.sendMessage("§cI hope you remember your PIN? If not run /setpin");

                }else{
                    player.sendMessage(prefix + "§cYou don't have enough money!");
                }

            }
            event.setCancelled(true);
        }

    }
}
