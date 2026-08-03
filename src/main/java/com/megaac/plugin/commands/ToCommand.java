package com.megaac.plugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Component.text("Использование: /to <ник>").color(NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Игрок не найден или не в сети.").color(NamedTextColor.RED));
            return true;
        }
        target.teleport(player.getLocation());
        target.sendMessage(Component.text("Вас телепортировал к себе " + player.getName()).color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(target.getName() + " телепортирован к вам.").color(NamedTextColor.GREEN));
        return true;
    }
}
