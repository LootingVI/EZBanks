package de.flori.ezbanks.commands;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SuspendCommand extends Command {

    public SuspendCommand() {
        super("bank-suspend");
        this.setPermission("ezbank.bank.suspend");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        if (!(sender instanceof Player player))
            return false;


            final SignGUI gui1;
            try {
                gui1 = SignGUI.builder()
                        .setLines(null, "§-----------", "§cType bank ID in first line", "§-----------")
                        .setType(Material.ACACIA_SIGN)
                        .setColor(DyeColor.GRAY)
                        .setHandler((p, result) -> {
                            final String input = result.getLineWithoutColor(0);

                            if (input.isEmpty()) {
                                p.sendMessage(EZBanks.getPrefix() + "§cPlease enter a correct bank ID!");
                                player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                return Collections.emptyList();
                            }

                            final BankAccount targetAccount = EZBanks.getInstance().getBankManager().getBankAccount(input);

                            if (targetAccount == null) {
                                p.sendMessage(Component.text(EZBanks.getPrefix() + "§cBank account not found!"));
                                player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                                return List.of();
                            }

                            boolean status = !targetAccount.isSuspended();

                            String message;

                            if(status){
                                //true
                                message = EZBanks.getPrefix() + "§aYou have successfully suspended the bank account §6" + input;
                            }else{
                                //false
                                message = EZBanks.getPrefix() + "§aYou have the bank account §6" + input + "§a successfully unsuspended!";
                            }


                            return List.of(
                                    SignGUIAction.run(() -> player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)),
                                    SignGUIAction.run(() -> EZBanks.getInstance().getBankManager().setSuspended(targetAccount, status)),
                                    SignGUIAction.run(() -> player.sendMessage(message))
                            );
                        })
                        .build();
            } catch (SignGUIVersionException e) {
                throw new RuntimeException(e);
            }

            gui1.open(player);

        return true;
    }

}
