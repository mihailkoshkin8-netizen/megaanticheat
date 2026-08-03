package com.megaac.plugin.commands;

import com.megaac.plugin.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MegaAcCommand implements CommandExecutor {

    private final Main plugin;

    public MegaAcCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.reloadPluginConfig();
            sender.sendMessage(Component.text("MegaAntiCheat: конфиг перезагружен.").color(NamedTextColor.GREEN));
            return true;
        }
        sender.sendMessage(Component.text("Использование: /megaac reload").color(NamedTextColor.YELLOW));
        return true;
    }
}
