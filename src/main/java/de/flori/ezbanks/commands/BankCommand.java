package de.flori.ezbanks.commands;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.gui.BankMenuGUI;
import de.flori.ezbanks.gui.BuyCardGUI;
import de.flori.ezbanks.gui.BuybankAccountGUI;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BankCommand extends Command {

    public BankCommand() {
        super("bank");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player player))
            return false;

        if (EZBanks.getInstance().getBankManager().hasBankAccount(player.getUniqueId())) {
            if (!ItemUtils.hasBankCard(player)) {
                player.openInventory(new BuyCardGUI().getInventory());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 1.0f);
            } else {
                final ItemStack itemStack = player.getInventory().getItemInMainHand();
                final BankAccount account = EZBanks.getInstance().getBankManager().getBankAccount(ItemUtils.getBankId(itemStack));
                if (ItemUtils.isBankCard(itemStack)) {

                    if(account.isSuspended()){
                        player.sendMessage(EZBanks.getPrefix() + "§cAccess to this account is blocked!");
                        player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                        return false;
                    }

                    player.openInventory(new BankMenuGUI().getInventory());
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                } else {
                    player.sendMessage(Component.text(EZBanks.getPrefix() + "§cNo bank card recognised! Please hold a bank card in your hand."));
                    player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                }
            }
        } else {
            if(EZBanks.getInstance().getConfigManager().getBankCost() != 0){
                player.openInventory(new BuybankAccountGUI().getInventory());
            }else{
                final String bankId = UUID.randomUUID().toString().split("-")[0];
                final int pin = ThreadLocalRandom.current().nextInt(1000, 10000);

                final BankAccount account = new BankAccount();
                account.setBankId(bankId);
                account.setOwnerUuid(player.getUniqueId());
                account.setPin(pin);

                EZBanks.getInstance().getBankManager().createBankAccount(account);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                player.sendMessage(Component.text(EZBanks.getPrefix() + "§aYou have successfully created a new account. Your bank account pin is: " + pin));
                player.sendMessage(Component.text("§cBut remember them well! You can't access your bank account without it!"));
                player.getInventory().addItem(ItemUtils.getBankCard(account));
            }

        }
        return true;
    }

}
