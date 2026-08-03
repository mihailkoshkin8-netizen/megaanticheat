package com.megaac.plugin.listeners;

import com.megaac.plugin.managers.DataManager;
import com.megaac.plugin.managers.PlayerData;
import com.megaac.plugin.util.AlertUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Слушает вход/выход игроков и рассылает стаффу подробную информацию:
 * ник, UUID, IP, пинг, бренд клиента.
 *
 * ВАЖНО: "бренд клиента" (channel minecraft:brand) НЕ является определением лаунчера.
 * Это просто строка, которую присылает клиент (обычно "vanilla" или имя мод-лоадера),
 * и читерский клиент может отправить любое значение по своему желанию. Достоверного
 * способа узнать лаунчер игрока на стороне сервера не существует.
 */
public class JoinListener implements Listener, PluginMessageListener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = DataManager.get(player.getUniqueId(), player.getName());
        data.name = player.getName();
        data.joinTime = System.currentTimeMillis();

        InetSocketAddress addr = player.getAddress();
        data.ip = addr;

        String ipStr = addr != null ? addr.getAddress().getHostAddress() : "неизвестно";

        Component msg = Component.text("Вход игрока: ").color(NamedTextColor.YELLOW)
                .append(Component.text(player.getName()).color(NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("  UUID: " + player.getUniqueId()).color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("  IP: " + ipStr).color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("  Пинг: " + player.getPing() + "мс").color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("  Бренд клиента: " + data.clientBrand + " (не путать с лаунчером)").color(NamedTextColor.GRAY));

        AlertUtil.broadcastAlert(msg);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        DataManager.remove(event.getPlayer().getUniqueId());
    }

    // ---- Получение "minecraft:brand" канала ----
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("minecraft:brand")) return;
        try {
            String brand = readMcString(message);
            PlayerData data = DataManager.get(player.getUniqueId(), player.getName());
            data.clientBrand = brand;
        } catch (IOException ignored) {
        }
    }

    /**
     * Minecraft передаёт строку как VarInt(длина в байтах) + UTF-8 байты.
     * Обычный DataInputStream#readUTF тут не подходит (он ждёт short-префикс),
     * поэтому парсим вручную.
     */
    private String readMcString(byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        int length = readVarInt(in);
        byte[] strBytes = new byte[length];
        in.readFully(strBytes);
        return new String(strBytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << position;
            if ((currentByte & 0x80) == 0) break;
            position += 7;
            if (position >= 32) throw new IOException("VarInt too big");
        }
        return value;
    }
}
