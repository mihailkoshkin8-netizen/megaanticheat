package com.megaac.plugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.UUID;

/**
 * Каждую секунду обновляет боковую панель модератору со сведениями
 * о наблюдаемом игроке: IP, ник, пинг, очки подозрения (xray/killaura/варны).
 */
public class SpecSidebarTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player moderator : Bukkit.getOnlinePlayers()) {
            UUID targetId = SpecManager.getTarget(moderator.getUniqueId());
            if (targetId == null) continue;
            Player target = Bukkit.getPlayer(targetId);
            if (target == null) continue;

            PlayerData data = DataManager.get(target.getUniqueId(), target.getName());

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager == null) continue;
            Scoreboard board = manager.getNewScoreboard();
            Objective obj = board.registerNewObjective("specinfo", "dummy", "§eНаблюдение");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            int line = 10;
            obj.getScore("§7Ник: §f" + target.getName()).setScore(line--);
            obj.getScore("§7Пинг: §f" + target.getPing()).setScore(line--);
            obj.getScore("§7IP: §f" + (data.ip != null ? data.ip.getAddress().getHostAddress() : "?")).setScore(line--);
            obj.getScore("§7Бренд: §f" + data.clientBrand).setScore(line--);
            obj.getScore("§7Варны чата: §f" + data.warnings).setScore(line--);
            obj.getScore("§7X-ray очки: §f" + data.oreSuspicionScore).setScore(line--);
            obj.getScore("§7Ударов/сек: §f" + data.recentHits.size()).setScore(line--);

            moderator.setScoreboard(board);
        }
    }
}
