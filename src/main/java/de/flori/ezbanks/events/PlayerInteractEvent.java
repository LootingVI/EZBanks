package de.flori.ezbanks.events;

import de.flori.ezbanks.utils.ItemUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerInteractEvent implements Listener {


    @EventHandler
    public void onPlayerUse(org.bukkit.event.player.PlayerInteractEvent event){
        Player p = event.getPlayer();
        if(ItemUtils.isBankCard(p.getItemInHand())){
            p.performCommand("bank");
            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        }
    }

}
