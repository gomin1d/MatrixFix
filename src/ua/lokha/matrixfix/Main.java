package ua.lokha.matrixfix;

import me.rerere.matrix.api.events.PlayerViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class Main extends JavaPlugin implements Listener {

    private List<String> onlyPlayerComponents = Collections.emptyList();
    private int pvpTimeSeconds = 10;
    private boolean debug = true;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getCommand("matrixfix").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);

        this.loadConfigData();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        this.loadConfigData();
    }

    private void loadConfigData() {
        onlyPlayerComponents = this.getConfig().getStringList("only-player-components");
        pvpTimeSeconds = this.getConfig().getInt("pvp-time-seconds");
        debug = this.getConfig().getBoolean("debug");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.reloadConfig();
        sender.sendMessage("§aКонфиг перезагружен.");
        return true;
    }

    private Map<Player, Long> lastPvpMap = new WeakHashMap<>();

    @EventHandler
    public void on(PlayerQuitEvent event) {
        lastPvpMap.remove(event.getPlayer());
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            lastPvpMap.put((Player) event.getDamager(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void on(PlayerViolationEvent event) {
        if (onlyPlayerComponents.contains(event.getComponent())) {
            Long lastPvp = lastPvpMap.get(event.getPlayer());
            long current = System.currentTimeMillis();
            if (lastPvp == null || ((current - lastPvp) > pvpTimeSeconds * 1000)) {
                this.getLogger().info("Отменено при пвп с мобом: " + event.getPlayer().getName() + " failed " + event.getHackType().name() +
                        " | " + event.getMessage() + " (+" + event.getViolations() + ")" +
                        " | component: "+ event.getComponent() +
                        " | last damage by player: " + (lastPvp == null ? "never" : ((double)(current - lastPvp) / 1000 + "s")));
                event.setCancelled(true);
            }
        }
    }
}
