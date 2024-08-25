package de.flori.ezbanks.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements BasicCommand {


    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] strings) {

        if (commandSourceStack instanceof Player) {

            Player player = (Player) commandSourceStack.getSender();

            player.sendMessage(ChatColor.RED + "/bank - Opens Bank GUI");
            player.sendMessage(ChatColor.RED + "/setpin - Allows you to change your pin (only works on your own card)");
        }
    }
}
