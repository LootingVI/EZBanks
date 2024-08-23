package de.flori.ezbanks.functions;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.BankManager;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CreateBankAccount {

   public CreateBankAccount(UUID id){
      String shortId = UUID.randomUUID().toString().split("-")[0];
      int randomNumber = ThreadLocalRandom.current().nextInt(1000, 10000);
      EZBanks.getInstance().bankManager().createBankAccount(new BankAccount(shortId, id, 0, randomNumber, false));

      String prefix = EZBanks.getInstance().configManager().getPrefix();

      Bukkit.getPlayer(id).sendMessage(prefix + "§aYou have successfully created a new account your bank account pin is:  " + randomNumber);
      Bukkit.getPlayer(id).sendMessage("§cBut remember them well! You can't access your bank account without it!");

      ItemStack ex = new ItemBuilder(Material.PAPER)
              .setDisplayName("§6Bank Card&7("+ shortId +")")
              .setLore("§r§7Bank Owner: " + Bukkit.getPlayer(id).getName())
              .setPersistentDataContainer("bankid", shortId)
              .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
              .build();

      Bukkit.getPlayer(id).getInventory().addItem(ex);

   }



}
