package com.megaac.plugin.managers;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Хранит, за кем наблюдает каждый модератор, и его состояние до входа в спектатор
 * (геймтмод, инвентарь, локация), чтобы можно было корректно вернуть его обратно.
 */
public class SpecManager {

    public static class SavedState {
        public GameMode gameMode;
        public Location location;
        public ItemStack[] inventory;
    }

    private static final Map<UUID, UUID> WATCHING = new HashMap<>(); // moderator -> target
    private static final Map<UUID, SavedState> SAVED = new HashMap<>();

    public static void startSpec(Player moderator, Player target) {
        SavedState state = new SavedState();
        state.gameMode = moderator.getGameMode();
        state.location = moderator.getLocation();
        state.inventory = moderator.getInventory().getContents();
        SAVED.put(moderator.getUniqueId(), state);
        WATCHING.put(moderator.getUniqueId(), target.getUniqueId());
    }

    public static SavedState stopSpec(Player moderator) {
        WATCHING.remove(moderator.getUniqueId());
        return SAVED.remove(moderator.getUniqueId());
    }

    public static boolean isSpectating(UUID moderator) {
        return WATCHING.containsKey(moderator);
    }

    public static UUID getTarget(UUID moderator) {
        return WATCHING.get(moderator);
    }
}
