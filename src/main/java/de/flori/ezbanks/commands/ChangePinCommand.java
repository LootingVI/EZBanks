package de.flori.ezbanks.commands;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.gui.BedrockForms.BedrockForms;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.ItemUtils;
import de.flori.ezbanks.utils.MessageUtils;
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
import org.bukkit.inventory.ItemStack;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ChangePinCommand extends Command {

    public ChangePinCommand() {
        super("setpin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player player))
            return false;

        if (!ItemUtils.isBankCard(player.getInventory().getItemInMainHand())) {
            player.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou must hold a bank card in your hand to change the pin."));
            player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
            return false;
        }

        if (!EZBanks.getInstance().getBankManager().hasBankAccount(player.getUniqueId())) {
            player.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou do not have a bank account."));
            player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
            return false;
        }

        final String bankId = ItemUtils.getBankId(player.getInventory().getItemInMainHand());
        final BankAccount bankAccount = EZBanks.getInstance().getBankManager().getBankAccount(bankId);

        if (bankAccount == null) {
            player.sendMessage(Component.text(EZBanks.getPrefix() + "§cThe bank account could not be found."));
            player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
            return false;
        }

        if (!bankAccount.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage(Component.text(EZBanks.getPrefix() + "§cYou can only change the pin of your own bank account."));
            player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
            return false;
        }

        final ItemStack itemStack = player.getInventory().getItemInMainHand();
        final BankAccount account = EZBanks.getInstance().getBankManager().getBankAccount(ItemUtils.getBankId(itemStack));
        if(account.isSuspended()){
            player.sendMessage(EZBanks.getPrefix() + "§cAccess to this account is blocked!");
            return false;
        }

        if(EZBanks.isBedrockSupportAvailable()) {

            if (EZBanks.getInstance().getFloodgateApi().isFloodgatePlayer(player.getUniqueId())) {
                FloodgatePlayer floodgatePlayer = EZBanks.getInstance().getFloodgateApi().getPlayer(player.getUniqueId());
                BedrockForms.sendChangePinForm(account, floodgatePlayer);
            }
        }

        final SignGUI gui1;
        try {
            gui1 = SignGUI.builder()
                    .setLines(null, "§-----------", "§cType new PIN in first line", "§-----------")
                    .setType(Material.ACACIA_SIGN)
                    .setColor(DyeColor.GRAY)
                    .setHandler((p, result) -> {
                        final String input = result.getLineWithoutColor(0);

                        if (input.isEmpty()) {
                            p.sendMessage(EZBanks.getPrefix() + "§cPlease enter a correct PIN!");
                            player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                            return Collections.emptyList();
                        }

                        if (!MessageUtils.isValidInteger(input)) {
                            p.sendMessage(EZBanks.getPrefix() + "§cThe PIN must be a number.");
                            player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                            return Collections.emptyList();
                        }

                        if (input.length() != 4) {
                            p.sendMessage(EZBanks.getPrefix() + "§cThe PIN must be exactly 4 digits long.");
                            player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                            return Collections.emptyList();
                        }

                        return List.of(
                                SignGUIAction.run(() -> player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f)),
                                SignGUIAction.run(() -> EZBanks.getInstance().getBankManager().setNewPin(bankAccount, Integer.parseInt(input))),
                                SignGUIAction.run(() -> player.sendMessage(EZBanks.getPrefix() + "§aYou have successfully changed the PIN to: §6" + input))
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
