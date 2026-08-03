package com.megaac.plugin.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Служебное состояние GUI /reports: какому слоту главного меню соответствует
 * какой игрок, и за каким игроком открыта подменю у конкретного зрителя.
 */
public class GuiSessionManager {

    private static final Map<UUID, Map<Integer, UUID>> MAIN_MENU_SLOTS = new HashMap<>();
    private static final Map<UUID, UUID> SUB_MENU_TARGET = new HashMap<>();

    public static void setMainMenuSlots(UUID viewer, Map<Integer, UUID> slotMap) {
        MAIN_MENU_SLOTS.put(viewer, slotMap);
    }

    public static UUID getTargetForSlot(UUID viewer, int slot) {
        Map<Integer, UUID> map = MAIN_MENU_SLOTS.get(viewer);
        return map != null ? map.get(slot) : null;
    }

    public static void setSubMenuTarget(UUID viewer, UUID target) {
        SUB_MENU_TARGET.put(viewer, target);
    }

    public static UUID getSubMenuTarget(UUID viewer) {
        return SUB_MENU_TARGET.get(viewer);
    }

    public static void clear(UUID viewer) {
        MAIN_MENU_SLOTS.remove(viewer);
        SUB_MENU_TARGET.remove(viewer);
    }
}
