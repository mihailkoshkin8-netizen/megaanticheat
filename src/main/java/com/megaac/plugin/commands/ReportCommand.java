package com.megaac.plugin.commands;

import com.megaac.plugin.managers.ReportManager;
import com.megaac.plugin.util.AlertUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class ReportCommand implements CommandExecutor {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        if (args.length < 2) {
            reporter.sendMessage(Component.text("Использование: /report <ник> <причина>").color(NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            reporter.sendMessage(Component.text("Игрок не найден или не в сети.").color(NamedTextColor.RED));
            return true;
        }
        if (target.getUniqueId().equals(reporter.getUniqueId())) {
            reporter.sendMessage(Component.text("Нельзя пожаловаться на самого себя.").color(NamedTextColor.RED));
            return true;
        }

        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        int id = counter.incrementAndGet();

        ReportManager.addReport(target.getUniqueId(), target.getName(), reporter.getName(), reason);

        Component alert = Component.text("Новый репорт #" + id + ": ").color(NamedTextColor.GOLD)
                .append(Component.text(reporter.getName()).color(NamedTextColor.WHITE))
                .append(Component.text(" пожаловался на ").color(NamedTextColor.GOLD))
                .append(Component.text(target.getName()).color(NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("  Причина: " + reason).color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("  [Наблюдать за игроком]").color(NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/spec " + target.getName())));

        AlertUtil.broadcastAlert(alert);
        reporter.sendMessage(Component.text("Жалоба #" + id + " отправлена модераторам.").color(NamedTextColor.GREEN));
        return true;
    }
}
