package com.Lsgserver;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class Lifesteal extends JavaPlugin implements Listener {

    private PlayerHeartsManager heartsManager;

    @Override
    public void onEnable() {
        this.heartsManager = new PlayerHeartsManager(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        heartsManager.loadHearts();
    }

    @Override
    public void onDisable() {
        heartsManager.saveHearts();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Opfer verliert ein Herz
        int victimHearts = heartsManager.changeHearts(victim.getUniqueId(), -2);
        setPlayerMaxHealth(victim, victimHearts);

        // Prüfen ob gebannt werden muss
        if (victimHearts < 2) {
            // 30 Tage Ban
            Date expire = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
            Bukkit.getBanList(BanList.Type.NAME).addBan(victim.getName(), "Alle Herzen verloren!", expire, null);
            victim.kickPlayer("Du hast alle Herzen verloren und bist für 30 Tage gebannt!");
        }

        // Killer bekommt ein Herz
        if (killer != null && killer != victim) {
            int killerHearts = heartsManager.changeHearts(killer.getUniqueId(), 2);
            setPlayerMaxHealth(killer, killerHearts);
        }
    }

    private void setPlayerMaxHealth(Player player, int health) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.max(2, health));
        if (player.getHealth() > player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        }
    }
}