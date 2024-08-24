package de.flori.ezbanks.events;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.EZBanks.*;
import de.flori.ezbanks.functions.OpenAmontForTransfer;
import de.flori.ezbanks.utils.ItemBuilder;
import de.flori.ezbanks.utils.MessageUtils;
import de.flori.ezbanks.utils.NumberUtils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Collections;
import java.util.List;

import static org.bukkit.persistence.PersistentDataType.STRING;


public class InventoryInteraction implements Listener {

    public Inventory inv = null;

    @EventHandler
    public void onInteract(InventoryClickEvent event) {
        try {

            Player player = (Player) event.getWhoClicked();

            NamespacedKey key = new NamespacedKey(EZBanks.getInstance(), "bankid");
            PersistentDataContainer container = player.getItemInHand().getItemMeta().getPersistentDataContainer();
            String bankId = container.get(key, STRING);
            double bal = EZBanks.getInstance().bankManager().getBankAccount(bankId).getBalance();
            String bankid1 = EZBanks.getInstance().bankManager().getBankAccount(player.getUniqueId()).getBankId();
            String symbol = EZBanks.getInstance().configManager().getSymbol();
            double balance = EZBanks.getEconomy().getBalance(player);
            int cardcost = EZBanks.getInstance().configManager().getCardCost();


            String prefix = EZBanks.getInstance().configManager().getPrefix();

            if (event.getView().getTitle().equals("§6Bank")) {

                if(event.getCurrentItem().getType() == Material.ARROW){
                    SignGUI gui = SignGUI.builder()
                            // set lines
                            .setLines(null, "§-----------", "§cType bankid in first line", "§-----------")

                            // set specific line, starting index is 0

                            // set the sign type
                            .setType(Material.ACACIA_SIGN)

                            // set the sign color
                            .setColor(DyeColor.GRAY)

                            // set the handler/listener (called when the player finishes editing)
                            .setHandler((p, result) -> {
                                // get a speficic line, starting index is 0
                                String line0 = result.getLineWithoutColor(0);

                                // get a specific line without color codes
                                String line1 = result.getLine(1);

                                // get all lines
                                String[] lines = result.getLines();

                                // get all lines without color codes
                                String[] linesWithoutColor = result.getLinesWithoutColor();



                                if (line0.isEmpty()) {
                                    p.sendMessage(prefix + "§cPlease enter a correct bank ID!");
                                    return Collections.emptyList();
                                }

/*
                                if (!MessageUtils.isValidDouble(line0)) {
                                    p.sendMessage(prefix + "Kein richtiger Betrag!2");
                                    return Collections.emptyList();
                                }
 */
                                if(EZBanks.getInstance().bankManager().getBankAccount(line0) == null){
                                    p.sendMessage(prefix + "§cPlease enter a correct bank ID!");
                                    return Collections.emptyList();
                                }else{

                                        return List.of(
                                                SignGUIAction.run(() -> new OpenAmontForTransfer().OpenAmountForTransfer(p, line0))
                                        );
                                }
                            }).build();

                    gui.open(player);
                }

                if(event.getCurrentItem().getType() == Material.ANVIL){
                    SignGUI gui = SignGUI.builder()
                            // set lines
                            .setLines(null, "§-----------", "§cType amount in first line", "§-----------")

                            // set specific line, starting index is 0

                            // set the sign type
                            .setType(Material.ACACIA_SIGN)

                            // set the sign color
                            .setColor(DyeColor.GRAY)

                            // set the handler/listener (called when the player finishes editing)
                            .setHandler((p, result) -> {
                                // get a speficic line, starting index is 0
                                String line0 = result.getLineWithoutColor(0);

                                // get a specific line without color codes
                                String line1 = result.getLine(1);

                                // get all lines
                                String[] lines = result.getLines();

                                // get all lines without color codes
                                String[] linesWithoutColor = result.getLinesWithoutColor();



                                if (line0.isEmpty()) {
                                    p.sendMessage(prefix + "§cPlease enter a correct amount!");
                                    return Collections.emptyList();
                                }

                                if (!MessageUtils.isValidDouble(line0)) {
                                    p.sendMessage(prefix + "§cPlease enter a correct amount!");
                                    return Collections.emptyList();
                                }
                                if (Double.parseDouble(line0) <= bal) {

                                    return List.of(
                                            SignGUIAction.run(() -> EZBanks.getEconomy().depositPlayer(player, Double.parseDouble(line0))),
                                            SignGUIAction.run(() -> EZBanks.getInstance().bankManager().removeBalance(bankId, Double.parseDouble(line0))),
                                            SignGUIAction.run(() -> player.sendMessage(prefix + "§aSuccessful debit of §6" + line0 + symbol))
                                    );


                                } else {
                                    p.sendMessage(prefix + "§cYou don't have enough money in your bank account!");
                                    return Collections.emptyList();

                                }
                            }).build();

                    gui.open(player);
                }

                if(event.getCurrentItem().getType() == Material.DISPENSER){
                    SignGUI gui = SignGUI.builder()
                            // set lines
                            .setLines(null, "§-----------", "§cType amount in first line", "§-----------")

                            // set specific line, starting index is 0

                            // set the sign type
                            .setType(Material.ACACIA_SIGN)

                            // set the sign color
                            .setColor(DyeColor.GRAY)

                            // set the handler/listener (called when the player finishes editing)
                            .setHandler((p, result) -> {
                                // get a speficic line, starting index is 0
                                String line0 = result.getLineWithoutColor(0);

                                // get a specific line without color codes
                                String line1 = result.getLine(1);

                                // get all lines
                                String[] lines = result.getLines();

                                // get all lines without color codes
                                String[] linesWithoutColor = result.getLinesWithoutColor();



                                if (line0.isEmpty()) {
                                    p.sendMessage(prefix + "§cPlease enter a correct amount!");
                                    return Collections.emptyList();
                                }

                                if (!MessageUtils.isValidDouble(line0)) {
                                    p.sendMessage(prefix + "§cPlease enter a correct amount!");
                                    return Collections.emptyList();
                                }
                                if (balance > Double.parseDouble(line0)) {

                                    return List.of(
                                            SignGUIAction.run(() -> EZBanks.getEconomy().withdrawPlayer(player, Double.parseDouble(line0))),
                                            SignGUIAction.run(() -> EZBanks.getInstance().bankManager().addBalance(bankId, Double.parseDouble(line0))),
                                            SignGUIAction.run(() -> player.sendMessage(prefix + "§aSuccessful bank transfer of §6" + line0 + symbol))
                                    );


                                } else {
                                    p.sendMessage(prefix + "§cYou don't have enough money!");
                                    return Collections.emptyList();

}
                            }).build();

                    gui.open(player);

                }

                event.setCancelled(true);
            }
        }catch (NullPointerException ignored){

        }
}
}



