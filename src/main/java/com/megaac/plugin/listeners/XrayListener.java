package com.megaac.plugin.listeners;

import com.megaac.plugin.managers.DataManager;
import com.megaac.plugin.managers.PlayerData;
import com.megaac.plugin.util.AlertUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * X-ray детектится эвристически: если игрок добывает ценную руду "вслепую" -
 * то есть ни один из соседних блоков не был воздухом/уже добытым блоком до этого
 * (типичный паттерн для x-ray: игрок роет прямо к руде через сплошной камень,
 * а не открывает пещеру/тоннель постепенно) - копим очки подозрения.
 *
 * Это НЕ стопроцентный детект (возможны ложные срабатывания на удачу), а сигнал
 * для стаффа, что стоит присмотреться к игроку через /spec.
 */
public class XrayListener implements Listener {

    private final boolean enabled;
    private final int threshold;
    private final long windowMillis;
    private final Set<Material> watchedOres;

    public XrayListener(boolean enabled, int threshold, int windowMinutes, List<String> watchedOreNames) {
        this.enabled = enabled;
        this.threshold = threshold;
        this.windowMillis = windowMinutes * 60_000L;
        this.watchedOres = watchedOreNames.stream()
                .map(name -> {
                    try {
                        return Material.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(m -> m != null)
                .collect(Collectors.toSet());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!enabled) return;
        Block block = event.getBlock();
        if (!watchedOres.contains(block.getType())) return;

        Player player = event.getPlayer();
        PlayerData data = DataManager.get(player.getUniqueId(), player.getName());

        long now = System.currentTimeMillis();
        if (now - data.oreWindowStart > windowMillis) {
            data.oreWindowStart = now;
            data.oreSuspicionScore = 0;
        }

        boolean wasExposed = false;
        for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Material neighbor = block.getRelative(face).getType();
            if (neighbor == Material.AIR || neighbor == Material.CAVE_AIR || neighbor.name().contains("TORCH")) {
                wasExposed = true;
                break;
            }
        }

        if (!wasExposed) {
            data.oreSuspicionScore++;
            if (data.oreSuspicionScore >= threshold) {
                AlertUtil.broadcastAlert(Component.text(player.getName() + " — подозрение X-Ray ")
                        .color(NamedTextColor.RED)
                        .append(Component.text("(" + data.oreSuspicionScore + " «слепых» добыч руды подряд)").color(NamedTextColor.GRAY)));
                data.oreSuspicionScore = 0;
                data.oreWindowStart = now;
            }
        }
    }
}
