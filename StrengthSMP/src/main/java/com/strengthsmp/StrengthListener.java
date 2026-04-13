package com.strengthsmp;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StrengthListener implements Listener {

    private final StrengthSMP plugin;
    private final StrengthManager manager;

    public StrengthListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.manager = plugin.getStrengthManager();
    }

    // ─── Player Kill → drop shell into killer's inventory ────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // ── Lose 1 strength on death (always, no floor) ──────────────────────
        int before = manager.getStrength(victim);
        manager.loseStrengthOnDeath(victim);
        int after = manager.getStrength(victim);
        victim.sendMessage(buildPrefix() + "§cYou lost 1 Strength on death. §7(" + after + "/" + StrengthManager.MAX_STRENGTH + ")");

        // ── Give shell to killer only if victim had positive strength ────────
        if (before <= 0) return; // Victim was at 0 or negative — no shell reward
        Entity killerEntity = victim.getKiller();
        if (!(killerEntity instanceof Player killer)) return;
        if (killer.getUniqueId().equals(victim.getUniqueId())) return; // No self-kill reward

        // Don't reward kills in creative/spectator (anti-exploit)
        if (killer.getGameMode() == GameMode.CREATIVE || killer.getGameMode() == GameMode.SPECTATOR) return;
        if (victim.getGameMode() == GameMode.CREATIVE || victim.getGameMode() == GameMode.SPECTATOR) return;

        // Give shell directly into killer's inventory (drop on ground if full)
        ItemStack shell = ShellItem.create();
        if (killer.getInventory().firstEmpty() == -1) {
            // Inventory full — drop at killer's feet
            killer.getWorld().dropItemNaturally(killer.getLocation(), shell);
            killer.sendMessage(buildPrefix() + "§eYou received a §6Strength §eshell! §7(Dropped — inventory full)");
        } else {
            killer.getInventory().addItem(shell);
            killer.sendMessage(buildPrefix() + "§eYou received a §6Strength §eshell! §7Right-click to absorb.");
        }
    }

    // ─── Right-click to consume shell ────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only fire once (main hand), ignore block interactions if holding shell
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Only on right-click (air or block)
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!ShellItem.isStrengthShell(item)) return;

        // Cancel event so the item doesn't do anything weird (e.g. open a block)
        event.setCancelled(true);

        int current = manager.getStrength(player);

        // Already at max — don't consume
        if (current >= StrengthManager.MAX_STRENGTH) {
            player.sendMessage(buildPrefix() + "§cYou are already at max Strength §7(" + StrengthManager.MAX_STRENGTH + "/" + StrengthManager.MAX_STRENGTH + ")§c!");
            return;
        }

        // Consume 1 shell
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        boolean success = manager.addStrength(player);
        if (success) {
            int newVal = manager.getStrength(player);
            player.sendMessage(buildPrefix() + "§a+1 Strength absorbed! §7(" + newVal + "/" + StrengthManager.MAX_STRENGTH + ")"
                    + buildBar(newVal, StrengthManager.MAX_STRENGTH));
        }
    }

    // ─── Login — re-apply attribute so relog can't remove the effect ─────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Slight delay to ensure player entity is fully loaded before we apply modifier
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                manager.reapplyOnJoin(event.getPlayer());
            }
        }, 2L);
    }

    // ─── Respawn — re-apply after respawn (entity refreshes on death) ─────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                manager.reapplyOnJoin(event.getPlayer());
            }
        }, 2L);
    }

    // ─── Quit — save immediately ──────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.saveData();
    }

    // ─── Utility ─────────────────────────────────────────────────────────────

    private String buildPrefix() {
        return "§6§l[Strength] §r";
    }

    private String buildBar(int current, int max) {
        StringBuilder bar = new StringBuilder(" §8[");
        // Show negative slots in red, empty as gray, filled as gold
        for (int i = -4; i <= max; i++) {
            if (i == 0) bar.append("§8|"); // divider at zero
            else if (i < 0) bar.append(i >= current ? "§c█" : "§7█");
            else bar.append(i <= current ? "§6█" : "§7█");
        }
        bar.append("§8]");
        return bar.toString();
    }
}
