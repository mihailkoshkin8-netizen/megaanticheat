package com.megaac.plugin.commands;

import com.megaac.plugin.managers.CheckingManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UncheckingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Использование: /unchecking <ник>").color(NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Игрок не найден или не в сети.").color(NamedTextColor.RED));
            return true;
        }
        if (!CheckingManager.isBeingChecked(target.getUniqueId())) {
            sender.sendMessage(Component.text("Этот игрок не проходит проверку.").color(NamedTextColor.RED));
            return true;
        }
        CheckingManager.stop(target.getUniqueId());
        target.clearTitle();
        target.sendMessage(Component.text("Проверка завершена, вы отпущены.").color(NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Игрок " + target.getName() + " отпущен с проверки.").color(NamedTextColor.GREEN));
        return true;
    }
}
