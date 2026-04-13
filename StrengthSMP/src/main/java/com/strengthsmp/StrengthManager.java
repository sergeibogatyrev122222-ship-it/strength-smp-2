package com.strengthsmp;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StrengthManager {

    public static final int MAX_STRENGTH = 5;
    // No minimum cap — strength can go infinitely negative

    // Unique key for our attack damage modifier so we never conflict with anything else
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("strengthsmp", "strength_damage");

    private final StrengthSMP plugin;
    private final Map<UUID, Integer> strengthData = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public StrengthManager(StrengthSMP plugin) {
        this.plugin = plugin;
        loadData();
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public int getStrength(Player player) {
        return strengthData.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Called when a player right-clicks and consumes a Strength shell.
     * Returns false if already at max (so we don't consume the item).
     */
    public boolean addStrength(Player player) {
        int current = getStrength(player);
        if (current >= MAX_STRENGTH) return false;
        setStrength(player, current + 1);
        return true;
    }

    /**
     * On death: always lose 1 strength, infinite downward (no floor).
     */
    public void loseStrengthOnDeath(Player player) {
        int current = getStrength(player);
        setStrength(player, current - 1);
    }

    /**
     * Admin hard reset to 0.
     */
    public void adminReset(Player player) {
        setStrength(player, 0);
    }

    /**
     * Player voluntary withdraw.
     * Returns false if amount is invalid or exceeds current positive strength.
     */
    public boolean withdrawStrength(Player player, int amount) {
        int current = getStrength(player);
        if (amount <= 0 || amount > current) return false;
        setStrength(player, current - amount);
        return true;
    }

    /**
     * Re-apply attribute on login to prevent bypass via relog.
     */
    public void reapplyOnJoin(Player player) {
        int points = getStrength(player);
        applyAttributeModifier(player, points);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    /**
     * Single source of truth. All strength changes go through here.
     * Only capped at MAX on the top end. No floor — can go infinitely negative.
     */
    private void setStrength(Player player, int value) {
        int clamped = Math.min(MAX_STRENGTH, value); // cap top only
        strengthData.put(player.getUniqueId(), clamped);
        applyAttributeModifier(player, clamped);
        saveData();
    }

    /**
     * Applies a generic.attack_damage attribute modifier.
     * Positive points = bonus damage. Negative points = damage penalty.
     * We always remove then re-add so there is never stacking or duplication.
     */
    public void applyAttributeModifier(Player player, int points) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr == null) return;

        // Remove any existing modifier with our key
        attr.getModifiers().stream()
                .filter(m -> m.key().equals(MODIFIER_KEY))
                .forEach(attr::removeModifier);

        if (points == 0) return;

        // ADD_NUMBER: positive = more damage, negative = less damage
        AttributeModifier modifier = new AttributeModifier(
                MODIFIER_KEY,
                points,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ANY
        );
        attr.addModifier(modifier);
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "strengths.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create strengths.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.getConfigurationSection("strengths") != null) {
            for (String key : dataConfig.getConfigurationSection("strengths").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int val = Math.min(MAX_STRENGTH, dataConfig.getInt("strengths." + key, 0));
                    strengthData.put(uuid, val);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveData() {
        for (Map.Entry<UUID, Integer> entry : strengthData.entrySet()) {
            dataConfig.set("strengths." + entry.getKey(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save strengths.yml: " + e.getMessage());
        }
    }
}
