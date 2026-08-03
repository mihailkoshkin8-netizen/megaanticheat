package com.megaac.plugin.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Отслеживает, кто из игроков сейчас находится "на проверке" (/cheking)
 * и какой модератор его вызвал.
 */
public class CheckingManager {

    // checkedPlayerUUID -> moderatorUUID
    private static final Map<UUID, UUID> ACTIVE = new ConcurrentHashMap<>();
    // checkedPlayerUUID -> контакт (discord/anydesk), который игрок написал в чат
    private static final Map<UUID, String> CONTACTS = new ConcurrentHashMap<>();

    public static void start(UUID checkedPlayer, UUID moderator) {
        ACTIVE.put(checkedPlayer, moderator);
    }

    public static void stop(UUID checkedPlayer) {
        ACTIVE.remove(checkedPlayer);
        CONTACTS.remove(checkedPlayer);
    }

    public static boolean isBeingChecked(UUID uuid) {
        return ACTIVE.containsKey(uuid);
    }

    public static UUID getModerator(UUID checkedPlayer) {
        return ACTIVE.get(checkedPlayer);
    }

    public static void setContact(UUID checkedPlayer, String contact) {
        CONTACTS.put(checkedPlayer, contact);
    }

    public static String getContact(UUID checkedPlayer) {
        return CONTACTS.get(checkedPlayer);
    }
}
