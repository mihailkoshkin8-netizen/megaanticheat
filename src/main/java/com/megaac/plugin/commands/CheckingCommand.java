package com.megaac.plugin.commands;

import com.megaac.plugin.managers.CheckingManager;
import com.megaac.plugin.managers.DataManager;
import com.megaac.plugin.managers.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;

public class CheckingCommand implements CommandExecutor {

    private final List<String> instructions;
    private final String titleText;
    private final String subtitleText;

    public CheckingCommand(List<String> instructions, String titleText, String subtitleText) {
        this.instructions = instructions;
        this.titleText = titleText;
        this.subtitleText = subtitleText;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player moderator)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        if (args.length < 1) {
            moderator.sendMessage(Component.text("Использование: /cheking <ник>  или  /cheking end <ник>").color(NamedTextColor.RED));
            return true;
        }

        // ---- Завершение проверки ----
        if (args[0].equalsIgnoreCase("end")) {
            if (args.length < 2) {
                moderator.sendMessage(Component.text("Использование: /cheking end <ник>").color(NamedTextColor.RED));
                return true;
            }
            Player endTarget = Bukkit.getPlayerExact(args[1]);
            if (endTarget == null) {
                moderator.sendMessage(Component.text("Игрок не найден или не в сети.").color(NamedTextColor.RED));
                return true;
            }
            if (!CheckingManager.isBeingChecked(endTarget.getUniqueId())) {
                moderator.sendMessage(Component.text("Этот игрок не проходит проверку.").color(NamedTextColor.RED));
                return true;
            }
            CheckingManager.stop(endTarget.getUniqueId());
            endTarget.clearTitle();
            endTarget.sendMessage(Component.text("Проверка завершена.").color(NamedTextColor.GREEN));
            moderator.sendMessage(Component.text("Вы завершили проверку игрока " + endTarget.getName()).color(NamedTextColor.GREEN));
            return true;
        }

        // ---- Начало проверки ----
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            moderator.sendMessage(Component.text("Игрок не найден или не в сети.").color(NamedTextColor.RED));
            return true;
        }
        if (CheckingManager.isBeingChecked(target.getUniqueId())) {
            moderator.sendMessage(Component.text("Этот игрок уже проходит проверку.").color(NamedTextColor.RED));
            return true;
        }

        CheckingManager.start(target.getUniqueId(), moderator.getUniqueId());

        target.showTitle(Title.title(
                Component.text(titleText.replace("&", "§")),
                Component.text(subtitleText.replace("&", "§")),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofMillis(500))
        ));

        for (String line : instructions) {
            target.sendMessage(Component.text(line.replace("&", "§")));
        }

        PlayerData data = DataManager.get(target.getUniqueId(), target.getName());
        Component info = Component.text("=== Проверка начата: " + target.getName() + " ===").color(NamedTextColor.GOLD)
                .append(Component.newline())
                .append(Component.text("Пинг: " + target.getPing()).color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("IP: " + (data.ip != null ? data.ip.getAddress().getHostAddress() : "?")).color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Бренд клиента: " + data.clientBrand).color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Игрок не может атаковать и не может получить урон, пока идёт проверка.").color(NamedTextColor.YELLOW));

        moderator.sendMessage(info);
        moderator.sendMessage(Component.text("Чтобы завершить проверку: /cheking end " + target.getName()).color(NamedTextColor.GRAY));

        return true;
    }
}
