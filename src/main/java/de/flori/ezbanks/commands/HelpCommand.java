package de.flori.ezbanks.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("bankhelp");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player player))
            return false;

        player.sendMessage(Component.text("§c/bank §8- §7Opens Bank GUI"));
        player.sendMessage(Component.text("§c/setpin §8- §7Allows you to change your pin (only works on your own card)"));
        player.sendMessage(Component.text("§c/bank-suspend §8- Allows to suspend/unsuspend bank accounts"));
        return true;
    }

}
