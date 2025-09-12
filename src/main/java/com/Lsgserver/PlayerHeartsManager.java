package com.Lsgserver;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHeartsManager {
    private final JavaPlugin plugin;
    private final File file;
    private final Map<UUID, Integer> hearts = new HashMap<>();

    public PlayerHeartsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "hearts.dat");
    }

    public int changeHearts(UUID uuid, int delta) {
        int current = hearts.getOrDefault(uuid, 20);
        int updated = Math.max(0, current + delta);
        hearts.put(uuid, updated);
        return updated;
    }

    public void loadHearts() {
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof UUID && entry.getValue() instanceof Integer) {
                        hearts.put((UUID) entry.getKey(), (Integer) entry.getValue());
                    }
                }
            }
        } catch (Exception ignored) {}
        // Setze Health für alle online Spieler
        for (Player p : Bukkit.getOnlinePlayers()) {
            int h = hearts.getOrDefault(p.getUniqueId(), 20);
            p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.max(2, h));
        }
    }

    public void saveHearts() {
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(hearts);
            }
        } catch (IOException ignored) {}
    }
}