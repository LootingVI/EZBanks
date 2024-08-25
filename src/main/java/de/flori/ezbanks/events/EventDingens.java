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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.bukkit.persistence.PersistentDataType.STRING;

public class EventDingens implements Listener {

    public Inventory inv = null;


    @EventHandler
    public void onPoo(InventoryClickEvent event){

        Player player = (Player) event.getWhoClicked();

        NamespacedKey key = new NamespacedKey(EZBanks.getInstance(), "bankid");
        //  PersistentDataContainer container = player.getItemInHand().getItemMeta().getPersistentDataContainer();
        // String bankId = container.get(key, STRING);
        // double bal = EZBanks.getInstance().bankManager().getBankAccount(bankId).getBalance();
        String bankid1 = EZBanks.getInstance().bankManager().getBankAccount(player.getUniqueId()).getBankId();
        String symbol = EZBanks.getInstance().configManager().getSymbol();
        double balance = EZBanks.getEconomy().getBalance(player);
        int cardcost = EZBanks.getInstance().configManager().getCardCost();
        String prefix = EZBanks.getInstance().configManager().getPrefix();

        if(event.getCurrentItem() == null)return;

        if(event.getView().getTitle().equals("§cBank menu")){
            if(event.getCurrentItem().getType() == Material.CHEST){
                if (player.getItemInHand().getType() == Material.PAPER) {

                    PersistentDataContainer container = player.getItemInHand().getItemMeta().getPersistentDataContainer();

                    if (container.has(key, PersistentDataType.STRING)) {
                        int pin = EZBanks.getInstance().bankManager().getBankAccount(container.get(key, PersistentDataType.STRING)).getPin();
                        double bbalance = EZBanks.getInstance().bankManager().getBankAccount(container.get(key, PersistentDataType.STRING)).getBalance();
                        UUID owner = EZBanks.getInstance().bankManager().getBankAccount(container.get(key, STRING)).getOwnerId();

                        String value = container.get(key, PersistentDataType.STRING);
                        SignGUI gui = SignGUI.builder()
                                // set lines
                                .setLines(null, "§7-----------", "§cType ur PIN in first line", "§7-----------")

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
                                        p.sendMessage(prefix + "§cThe PIN is not correct!");
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
                                                .setLore("§e§bID: §r§6"+ container.get(key, PersistentDataType.STRING) , "§e§bBalance: §r§a" +  bbalance + symbol)
                                                .build();
                                        //ItemStack depositAll = new ItemBuilder(Material.DISPENSER)
                                        //      .setDisplayName("§e§bDeposit all Money")
                                        //    .setLore("§k§eM§r§7Deposit &cALL &7Money!")
                                        //    .build();
                                        ItemStack deposit = new ItemBuilder(Material.ANVIL)
                                                .setDisplayName("§e§bDeposit a custom amount of Money")
                                                .setLore("§7Deposit a set amount of Money")
                                                .build();
                                        //ItemStack withdrawAll = new ItemBuilder(Material.DISPENSER)
                                        //       .setDisplayName("§e§bDeposit a custom amount of Money")
                                        //      .setLore("§k§eM§r§7Withdraw all Money")
                                        //      .build();
                                        ItemStack frame = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                                                .setDisplayName(" ")
                                                .build();
                                        ItemStack withdraw = new ItemBuilder(Material.DISPENSER)
                                                .setDisplayName("§e§bWithdraw a custom amount of Money")
                                                .setLore("§7Withdraw a custom amount of money")
                                                .build();

                                        //Auszug
                                        ItemStack accountStatement = new ItemBuilder(Material.PAPER)
                                                .setDisplayName("§6§bAccount Statement")
                                                .setLore("§7Prints out the last 10 transactions   §cComing Soon TM")
                                                .build();

                                        this.inv.setItem(0, frame);
                                        this.inv.setItem(1, frame);
                                        this.inv.setItem(2, frame);
                                        this.inv.setItem(3, frame);
                                        this.inv.setItem(4, frame);
                                        this.inv.setItem(5, frame);
                                        this.inv.setItem(6, frame);
                                        this.inv.setItem(7, frame);
                                        this.inv.setItem(8, frame);
                                        this.inv.setItem(9, frame);
                                        this.inv.setItem(17, frame);
                                        this.inv.setItem(18, frame);
                                        this.inv.setItem(26, frame);
                                        this.inv.setItem(27, frame);
                                        this.inv.setItem(35, frame);
                                        this.inv.setItem(36, frame);
                                        this.inv.setItem(37, frame);
                                        this.inv.setItem(38, frame);
                                        this.inv.setItem(39, frame);
                                        this.inv.setItem(40, frame);
                                        this.inv.setItem(41, frame);
                                        this.inv.setItem(42, frame);
                                        this.inv.setItem(43, frame);
                                        this.inv.setItem(44, frame);


                                        this.inv.setItem(10, transfer);
                                        //this.inv.setItem(1, quickTransfer);
                                        //this.inv.setItem(7, depositAll);
                                        this.inv.setItem(16, deposit);
                                        this.inv.setItem(22, info);
                                        //this.inv.setItem(43, withdrawAll);
                                        this.inv.setItem(28, withdraw);
                                        this.inv.setItem(34, accountStatement);


                                        return List.of(
                                                SignGUIAction.openInventory(EZBanks.getInstance(), inv)
                                        );
                                    } else {
                                        p.sendMessage(prefix + "§cThe PIN is not correct!");
                                        return Collections.emptyList();
                                    }

                                }).build();

                        gui.open(player);

                    }

                } else {
                    player.sendMessage(prefix + "§cNo bank card recognised! Please hold a bank card in your hand.");
                }
                event.setCancelled(true);
            }

            if(event.getCurrentItem().getType() == Material.NAME_TAG){
                Bukkit.getPlayer(player.getUniqueId()).performCommand("setpin");
                event.setCancelled(true);
            }
        }
    }

}
