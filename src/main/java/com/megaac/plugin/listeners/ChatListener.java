package com.megaac.plugin.listeners;

import com.megaac.plugin.managers.CheckingManager;
import com.megaac.plugin.managers.DataManager;
import com.megaac.plugin.managers.PlayerData;
import com.megaac.plugin.util.AlertUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final List<String> badWords;
    private final String censorSymbol;
    private final String alertPermission;

    public ChatListener(List<String> badWords, String censorSymbol, String alertPermission) {
        this.badWords = badWords;
        this.censorSymbol = censorSymbol;
        this.alertPermission = alertPermission;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        UUID senderId = sender.getUniqueId();
        String raw = PlainTextComponentSerializer.plainText().serialize(event.message());

        // ---- Игрок находится "на проверке" (/cheking): его сообщения видит только вызвавший модератор ----
        if (CheckingManager.isBeingChecked(senderId)) {
            event.setCancelled(true);

            UUID modId = CheckingManager.getModerator(senderId);
            Player mod = modId != null ? Bukkit.getPlayer(modId) : null;

            // Если игрок ещё не указал контакт - считаем первое сообщение контактом
            if (CheckingManager.getContact(senderId) == null) {
                CheckingManager.setContact(senderId, raw);
            }

            if (mod != null) {
                mod.sendMessage(Component.text("[Проверка] " + sender.getName() + ": " + raw)
                        .color(NamedTextColor.AQUA));
            }
            return;
        }

        // ---- Обычная модерация мата ----
        String lower = raw.toLowerCase();
        boolean hasBadWord = false;
        for (String bad : badWords) {
            if (bad.isBlank()) continue;
            if (lower.contains(bad.toLowerCase())) {
                hasBadWord = true;
                break;
            }
        }

        if (!hasBadWord) return; // обычное сообщение, ничего не трогаем

        // Варн игроку
        PlayerData data = DataManager.get(senderId, sender.getName());
        data.warnings++;

        String censored = censor(raw, badWords, censorSymbol);

        // Отменяем стандартную отправку и рассылаем сами: обычным - censored, стаффу - оригинал
        event.setCancelled(true);
        Component censoredComp = Component.text("<" + sender.getName() + "> " + censored);
        Component originalComp = Component.text("<" + sender.getName() + "> " + raw).color(NamedTextColor.GRAY);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.hasPermission(alertPermission)) {
                viewer.sendMessage(originalComp);
            } else {
                viewer.sendMessage(censoredComp);
            }
        }
        Bukkit.getConsoleSender().sendMessage(originalComp);

        AlertUtil.broadcastAlert(Component.text(sender.getName() + " ругается в чате (варн #" + data.warnings + "): ")
                .color(NamedTextColor.GOLD)
                .append(Component.text(raw).color(NamedTextColor.WHITE)));
    }

    private String censor(String text, List<String> badWords, String symbol) {
        String result = text;
        for (String bad : badWords) {
            if (bad.isBlank()) continue;
            Pattern pattern = Pattern.compile(Pattern.quote(bad), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(result);
            StringBuilder sb = new StringBuilder();
            int last = 0;
            while (matcher.find()) {
                sb.append(result, last, matcher.start());
                sb.append(symbol.repeat(Math.max(1, matcher.end() - matcher.start())));
                last = matcher.end();
            }
            sb.append(result.substring(last));
            result = sb.toString();
        }
        return result;
    }
}
