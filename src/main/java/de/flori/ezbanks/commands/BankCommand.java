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

import static org.apache.logging.log4j.LogManager.getLogger;
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

            if (player.getItemInHand().getType() == Material.PAPER) {

                NamespacedKey key = new NamespacedKey(EZBanks.getInstance(), "bankid");

                PersistentDataContainer container = player.getItemInHand().getItemMeta().getPersistentDataContainer();

                if (container.has(key, PersistentDataType.STRING)) {
                    int pin = EZBanks.getInstance().bankManager().getBankAccount(container.get(key, PersistentDataType.STRING)).getPin();
                    double balance = EZBanks.getInstance().bankManager().getBankAccount(container.get(key, PersistentDataType.STRING)).getBalance();
                    UUID owner = EZBanks.getInstance().bankManager().getBankAccount(container.get(key, STRING)).getOwnerId();

                    String value = container.get(key, PersistentDataType.STRING);
                    SignGUI gui = SignGUI.builder()
                            // set lines
                            .setLines(null, "§7-----------", "§cType ur pin in first line", "§7-----------")

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
                                    p.sendMessage(prefix + "§cThe pin is not correct!");
                                    return Collections.emptyList();
                                }

                                if (line0.equals(String.valueOf(pin))) {
                                    this.inv = player.getServer().createInventory(null, 45, "§6Bank");


                                    ItemStack transfer = new ItemBuilder(Material.ARROW)
                                            .setDisplayName("§e§bSend Money")
                                            .setLore("§7Send Money to another bank account!")
                                            .build();
                                    //ItemStack quickTransfer = new ItemBuilder(Material.TIPPED_ARROW)
                                     //       .setDisplayName("§e§bSend Money (instantly)")
                                     //       .setLore("§k§eM§r§7Send Money to another bank account &b&cINSTANTLY!", "", "§7Arrival Time: A Few Ticks", "", "§7You will be charged §6§b12‰§r§6 Transaction Taxes!")
                                        //    .build();
                                    ItemStack info = new ItemBuilder(Material.KNOWLEDGE_BOOK)
                                        .setDisplayName("§6§bOwner: &r&f" + Bukkit.getOfflinePlayer(owner).getName())
                                            .setLore("§e§bID: §r§6"+ container.get(key, PersistentDataType.STRING) , "§e§bBalance: §r§a" +  balance + symbol)
                                            .build();
                                    //ItemStack depositAll = new ItemBuilder(Material.DISPENSER)
                                      //      .setDisplayName("§e§bDeposit all Money")
                                        //    .setLore("§k§eM§r§7Deposit &cALL &7Money!")
                                        //    .build();
                                    ItemStack deposit = new ItemBuilder(Material.DISPENSER)
                                            .setDisplayName("§e§bDeposit a custom amount of Money")
                                            .setLore("§7Deposit a set amount of Money")
                                            .build();
                                    //ItemStack withdrawAll = new ItemBuilder(Material.DISPENSER)
                                     //       .setDisplayName("§e§bDeposit a custom amount of Money")
                                      //      .setLore("§k§eM§r§7Withdraw all Money")
                                      //      .build();
                                    ItemStack withdraw = new ItemBuilder(Material.ANVIL)
                                          .setDisplayName("§e§bWithdraw a custom amount of Money")
                                          .setLore("§7Withdraw a custom amount of money")
                                          .build();


                                    this.inv.setItem(0, transfer);
                                    //this.inv.setItem(1, quickTransfer);
                                    //this.inv.setItem(7, depositAll);
                                    this.inv.setItem(8, deposit);
                                    this.inv.setItem(22, info);
                                    //this.inv.setItem(43, withdrawAll);
                                    this.inv.setItem(44, withdraw);


                                    return List.of(
                                            SignGUIAction.openInventory(EZBanks.getInstance(), inv)
                                    );
                                } else {
                                    p.sendMessage(prefix + "§cThe pin is not correct!");
                                    return Collections.emptyList();
                                }

                            }).build();

                    gui.open(player);

                }

            } else {
                player.sendMessage(prefix + "§cNo bank card recognised! Please hold a bank card in your hand.");
            }

        }
    }
}
