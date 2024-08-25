package de.flori.ezbanks.commands;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.utils.MessageUtils;
import de.flori.ezbanks.utils.NumberUtils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChangePinCommand implements BasicCommand {
    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] strings) {

        Player player = (Player) commandSourceStack.getSender();

        NamespacedKey key = new NamespacedKey(EZBanks.getInstance(), "bankid");

        PersistentDataContainer container = player.getItemInHand().getItemMeta().getPersistentDataContainer();

        String prefix = EZBanks.getInstance().configManager().getPrefix();
        String symbol = EZBanks.getInstance().configManager().getSymbol();
        String bankid1 = EZBanks.getInstance().bankManager().getBankAccount(player.getUniqueId()).getBankId();
        UUID owner = EZBanks.getInstance().bankManager().getBankAccount(container.get(key, PersistentDataType.STRING)).getOwnerId();

        if (bankid1 == null) {
            player.sendMessage(prefix + "§cYou do not yet have a bank account. Use /bank to create one.");
        }

        if (player.getItemInHand().getType() == Material.PAPER) {

            if (owner.equals(player.getUniqueId())) {

                SignGUI gui1 = SignGUI.builder()
                        // set lines
                        .setLines(null, "§-----------", "§cType new PIN in first line", "§-----------")

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
                            if (MessageUtils.isValidInteger(line0)){
                                if (line0.length() == 4) {
                                    return List.of(
                                            SignGUIAction.run(() -> EZBanks.getInstance().bankManager().setNewPin(bankid1, Integer.parseInt(line0))),
                                            SignGUIAction.run(() -> player.sendMessage(prefix + "§aYou have successfully changed the PIN to: §6" + line0))

                                );


                            }else {
                                p.sendMessage(prefix + "§cIt must be a number with the length of 4!");
                                return Collections.emptyList();
                                }
                            }else {
                                p.sendMessage(prefix + "§cIt must be a number with the length of 4!");
                                return Collections.emptyList();

                            }
                        }).build();

                gui1.open(player);

            } else {
                player.sendMessage(prefix + "§cUnfortunately, this is not your account, so you cannot change the pin.");
            }

                } else {
                    player.sendMessage(prefix + "§cNo bank card recognised! Please hold a bank card in your hand.");
                }
            }
        }
