package com.megaac.plugin.commands;

import com.megaac.plugin.managers.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        boolean nowVanished = VanishManager.toggle(player);
        if (nowVanished) {
            player.sendMessage(Component.text("Ванишь включён. Вас видят только игроки с правом megaac.staff.").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Ванишь выключен.").color(NamedTextColor.YELLOW));
        }
        return true;
    }
}
