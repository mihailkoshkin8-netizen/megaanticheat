package com.megaac.plugin;

import com.megaac.plugin.commands.*;
import com.megaac.plugin.listeners.*;
import com.megaac.plugin.managers.SpecSidebarTask;
import com.megaac.plugin.util.AlertUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Main extends JavaPlugin {

    private JoinListener joinListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        joinListener = new JoinListener();
        reloadPluginConfig();

        // Команды
        getCommand("cheking").setExecutor(new CheckingCommand(
                getConfig().getStringList("checking.instructions"),
                getConfig().getString("checking.title", "&c&lПРОВЕРКА НА ЧИТЫ"),
                getConfig().getString("checking.subtitle", "&7Свяжитесь с модератором")
        ));
        getCommand("unchecking").setExecutor(new UncheckingCommand());
        getCommand("spec").setExecutor(new SpecCommand());
        SpecOffCommand specOffCommand = new SpecOffCommand();
        getCommand("specoff").setExecutor(specOffCommand);
        getCommand("unspec").setExecutor(specOffCommand); // алиас
        getCommand("v").setExecutor(new VanishCommand());
        getCommand("reports").setExecutor(new ReportsCommand());
        getCommand("tp").setExecutor(new TpCommand());
        getCommand("to").setExecutor(new ToCommand());
        getCommand("report").setExecutor(new ReportCommand());
        getCommand("megaac").setExecutor(new MegaAcCommand(this));

        // Плагин-канал для получения бренда клиента
        getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", joinListener);

        // Периодическое обновление боковой панели у наблюдающих модераторов
        new SpecSidebarTask().runTaskTimer(this, 20L, 20L);

        getLogger().info("MegaAntiCheat включен.");
    }

    /**
     * Пересобирает слушателей боя/чата/xray на основе текущего config.yml
     * (используется и при onEnable, и при /megaac reload).
     */
    public void reloadPluginConfig() {
        FileConfiguration cfg = getConfig();

        String alertPermission = cfg.getString("alert-permission", "megaac.staff");
        String prefix = cfg.getString("messages.prefix", "&8[&cMegaAC&8] &r").replace("&", "§");
        AlertUtil.init(alertPermission, prefix);
        com.megaac.plugin.managers.VanishManager.init(this, alertPermission);

        // Пересоздаём JoinListener один раз (он же PluginMessageListener) - если его ещё нет
        if (joinListener == null) {
            joinListener = new JoinListener();
        }

        // Снимаем и заново вешаем все листенеры этого плагина, чтобы reload подхватил новые значения конфига
        org.bukkit.event.HandlerList.unregisterAll(this);
        getServer().getPluginManager().registerEvents(joinListener, this);
        getServer().getPluginManager().registerEvents(new SpecItemListener(), this);
        getServer().getPluginManager().registerEvents(new VanishJoinListener(), this);
        getServer().getPluginManager().registerEvents(new ReportsGuiListener(), this);

        List<String> badWords = cfg.getStringList("chat.bad-words");
        String censorSymbol = cfg.getString("chat.censor-symbol", "*");
        getServer().getPluginManager().registerEvents(new ChatListener(badWords, censorSymbol, alertPermission), this);

        boolean killauraEnabled = cfg.getBoolean("checks.killaura.enabled", true);
        int maxCps = cfg.getInt("checks.killaura.max-cps", 20);
        double maxAngle = cfg.getDouble("checks.killaura.max-angle-diff", 40.0);
        long multiAuraWindow = cfg.getLong("checks.killaura.multi-aura-window-ms", 150);
        boolean reachEnabled = cfg.getBoolean("checks.reach.enabled", true);
        double maxReach = cfg.getDouble("checks.reach.max-distance", 3.1);
        getServer().getPluginManager().registerEvents(
                new CombatListener(killauraEnabled, maxCps, maxAngle, multiAuraWindow, reachEnabled, maxReach), this);

        boolean xrayEnabled = cfg.getBoolean("checks.xray.enabled", true);
        int xrayThreshold = cfg.getInt("checks.xray.suspicion-threshold", 6);
        int xrayWindowMinutes = cfg.getInt("checks.xray.ore-window-minutes", 10);
        List<String> watchedOres = cfg.getStringList("checks.xray.watched-ores");
        getServer().getPluginManager().registerEvents(
                new XrayListener(xrayEnabled, xrayThreshold, xrayWindowMinutes, watchedOres), this);
    }
}
