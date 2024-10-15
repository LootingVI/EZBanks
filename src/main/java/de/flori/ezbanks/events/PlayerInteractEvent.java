package de.flori.ezbanks.events;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.ItemUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractEvent implements Listener {


    @EventHandler
    public void onPlayerUse(org.bukkit.event.player.PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(ItemUtils.isBankCard(player.getItemInHand())){

            final ItemStack itemStack = player.getInventory().getItemInMainHand();
            final BankAccount account = EZBanks.getInstance().getBankManager().getBankAccount(ItemUtils.getBankId(itemStack));

            if(account.isSuspended()){
                player.sendMessage(EZBanks.getPrefix() + "§cAccess to this account is blocked!");
                player.playSound(player.getLocation(), Sound.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0f, 1.0f);
                return;
            }


            player.performCommand("bank");
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        }
    }

}
