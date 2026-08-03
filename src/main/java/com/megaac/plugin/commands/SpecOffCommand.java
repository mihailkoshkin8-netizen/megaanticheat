package com.megaac.plugin.commands;

import com.megaac.plugin.managers.SpecManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpecOffCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player moderator)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        if (!SpecManager.isSpectating(moderator.getUniqueId())) {
            moderator.sendMessage(Component.text("Вы не в режиме наблюдения.").color(NamedTextColor.RED));
            return true;
        }

        SpecManager.SavedState state = SpecManager.stopSpec(moderator);
        if (state != null) {
            moderator.setGameMode(state.gameMode);
            moderator.teleport(state.location);
            moderator.getInventory().setContents(state.inventory);
        }
        moderator.setSpectatorTarget(null);
        moderator.sendMessage(Component.text("Наблюдение завершено.").color(NamedTextColor.GREEN));
        return true;
    }
}
