package de.flori.ezbanks.utils;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.impl.BankAccount;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Objects;

@UtilityClass
public class ItemUtils {

    public NamespacedKey BANK_CARD_KEY = new NamespacedKey(EZBanks.getInstance(), "bankId");

    public boolean isBankCard(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(BANK_CARD_KEY, PersistentDataType.STRING);
    }

    public boolean hasBankCard(Player player) {
        return Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).anyMatch(ItemUtils::isBankCard);
    }

    public String getBankId(ItemStack itemStack) {
        if (!isBankCard(itemStack)) return null;
        return itemStack.getItemMeta().getPersistentDataContainer().get(BANK_CARD_KEY, PersistentDataType.STRING);
    }

    public ItemStack getBankCard(BankAccount account) {
        return new ItemBuilder(Material.PAPER)
                .setDisplayName("§6Bank Card §7(" + account.getBankId() + ")")
                .setLore("§r§7Bank Owner: " + Bukkit.getOfflinePlayer(account.getOwnerUuid()).getName())
                .setPersistentDataContainer(BANK_CARD_KEY, account.getBankId())
                .build();
    }

}
