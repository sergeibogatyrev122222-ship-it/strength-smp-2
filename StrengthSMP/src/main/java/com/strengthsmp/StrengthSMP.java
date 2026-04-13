package com.strengthsmp;

import org.bukkit.plugin.java.JavaPlugin;

public class StrengthSMP extends JavaPlugin {

    private StrengthManager strengthManager;

    @Override
    public void onEnable() {
        strengthManager = new StrengthManager(this);

        getServer().getPluginManager().registerEvents(new StrengthListener(this), this);

        StrengthCommand cmd = new StrengthCommand(this);
        getCommand("strength").setExecutor(cmd);
        getCommand("strength").setTabCompleter(cmd);

        getLogger().info("StrengthSMP v1.0.0 enabled!");
    }

    @Override
    public void onDisable() {
        if (strengthManager != null) {
            strengthManager.saveData();
        }
        getLogger().info("StrengthSMP disabled.");
    }

    public StrengthManager getStrengthManager() {
        return strengthManager;
    }
}
