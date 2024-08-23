package de.flori.ezbanks.functions;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.utils.MessageUtils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.bukkit.persistence.PersistentDataType.STRING;

public class OpenAmontForTransfer {

    public void OpenAmountForTransfer(Player player, String bid){

        NamespacedKey key = new NamespacedKey(EZBanks.getInstance(), "bankid");
        PersistentDataContainer container = player.getItemInHand().getItemMeta().getPersistentDataContainer();
        String bankId = container.get(key, STRING);
        double bal = EZBanks.getInstance().bankManager().getBankAccount(bankId).getBalance();
        String prefix = EZBanks.getInstance().configManager().getPrefix();
        String symbol = EZBanks.getInstance().configManager().getSymbol();


        SignGUI gui1 = SignGUI.builder()
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
                    String[] linesWithoutColor1 = result.getLinesWithoutColor();



                    if (line0.isEmpty()) {
                        p.sendMessage(prefix + "§cPlease enter a correct amount!");
                        return Collections.emptyList();
                    }

                    if (!MessageUtils.isValidDouble(line0)) {
                        p.sendMessage(prefix + "§cPlease enter a correct amount!");
                        return Collections.emptyList();
                    }
                    double balance = EZBanks.getEconomy().getBalance(player);
                    if (Double.parseDouble(line0) <= bal) {

                        UUID owner = EZBanks.getInstance().bankManager().getBankAccount(bid).getOwnerId();

                        if(Bukkit.getPlayer(owner) != null){
                            Bukkit.getOfflinePlayer(owner).getPlayer().sendMessage(prefix + "§aYou have received a bank transfer from §b" + p.getName() + "§a. Amount: §6" + line0 + symbol);
                        }

                        return List.of(
                                SignGUIAction.run(() -> EZBanks.getInstance().bankManager().removeBalance(bankId, Double.parseDouble(line0))),
                                SignGUIAction.run(() -> EZBanks.getInstance().bankManager().addBalance(bid, Double.parseDouble(line0))),
                                SignGUIAction.run(() -> player.sendMessage(prefix + "§aYou have successfully transferred §6" + line0 + symbol +  " §ato §b" + Bukkit.getOfflinePlayer(owner).getName()))

                        );


                    } else {
                        p.sendMessage(prefix + "§cYou don't have enough money in your bank account!");
                        return Collections.emptyList();

                    }
                }).build();

        gui1.open(player);

    }

}
