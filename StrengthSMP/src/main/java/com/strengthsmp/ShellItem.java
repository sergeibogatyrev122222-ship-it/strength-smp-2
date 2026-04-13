package com.strengthsmp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ShellItem {

    // NBT tag so we can reliably identify our custom shell
    public static final NamespacedKey SHELL_KEY = new NamespacedKey("strengthsmp", "strength_shell");

    /**
     * Creates a single Strength shell item.
     */
    public static ItemStack create() {
        ItemStack shell = new ItemStack(Material.NAUTILUS_SHELL, 1);
        ItemMeta meta = shell.getItemMeta();

        // Name: "Strength" in gold, no italic
        meta.displayName(
                Component.text("Strength")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
        );

        // Lore
        meta.lore(List.of(
                Component.text("Right-click to absorb.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("+1 Attack Damage")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        // Mark with NBT so we can identify it reliably (not by name, which could be spoofed)
        meta.getPersistentDataContainer().set(SHELL_KEY, PersistentDataType.BYTE, (byte) 1);

        shell.setItemMeta(meta);
        return shell;
    }

    /**
     * Returns true if the given ItemStack is our custom Strength shell.
     * Checks NBT tag — not display name — so it cannot be spoofed with renamed items.
     */
    public static boolean isStrengthShell(ItemStack item) {
        if (item == null || item.getType() != Material.NAUTILUS_SHELL) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(SHELL_KEY, PersistentDataType.BYTE);
    }
}
