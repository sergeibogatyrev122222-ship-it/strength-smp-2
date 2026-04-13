package com.strengthsmp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StrengthCommand implements CommandExecutor, TabCompleter {

    private final StrengthSMP plugin;
    private final StrengthManager manager;

    public StrengthCommand(StrengthSMP plugin) {
        this.plugin = plugin;
        this.manager = plugin.getStrengthManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /strength — check own strength
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command without arguments.");
                return true;
            }
            int strength = manager.getStrength(player);
            int max = StrengthManager.MAX_STRENGTH;
            sender.sendMessage(prefix() + "§eYour Strength: §6" + strength + "§7/§6" + max
                    + buildBar(strength, max));
            sender.sendMessage(prefix() + "§7Each point adds §c+1 attack damage§7.");
            return true;
        }

        String sub = args[0].toLowerCase();

        // /strength withdraw <amount>
        if (sub.equals("withdraw")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can withdraw strength.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /strength withdraw <amount>");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number.");
                return true;
            }

            if (amount <= 0) {
                sender.sendMessage("§cAmount must be greater than 0.");
                return true;
            }

            int current = manager.getStrength(player);
            if (current == 0) {
                sender.sendMessage(prefix() + "§cYou have no strength to withdraw.");
                return true;
            }
            if (amount > current) {
                sender.sendMessage(prefix() + "§cYou only have §e" + current + "§c strength. Cannot withdraw §e" + amount + "§c.");
                return true;
            }

            manager.withdrawStrength(player, amount);
            int newVal = manager.getStrength(player);
            sender.sendMessage(prefix() + "§eWithdrew §c" + amount + "§e strength. Now at §6" + newVal
                    + "§7/§6" + StrengthManager.MAX_STRENGTH + buildBar(newVal, StrengthManager.MAX_STRENGTH));
            return true;
        }

        // /strength reset <player>
        if (sub.equals("reset")) {
            if (!sender.hasPermission("strengthsmp.admin")) {
                sender.sendMessage("§cYou don't have permission to do that.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /strength reset <player>");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer §e" + args[1] + " §cis not online.");
                return true;
            }

            manager.adminReset(target);
            sender.sendMessage(prefix() + "§eReset §c" + target.getName() + "§e's strength to §60§e.");
            target.sendMessage(prefix() + "§cAn admin has reset your strength to 0.");
            return true;
        }

        // Unknown
        sender.sendMessage("§cUsage:");
        sender.sendMessage("§7  /strength §8— Check your strength");
        sender.sendMessage("§7  /strength withdraw <amount> §8— Remove strength");
        if (sender.hasPermission("strengthsmp.admin")) {
            sender.sendMessage("§7  /strength reset <player> §8— Reset a player's strength");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String p = args[0].toLowerCase();
            if ("withdraw".startsWith(p)) completions.add("withdraw");
            if (sender.hasPermission("strengthsmp.admin") && "reset".startsWith(p)) completions.add("reset");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset") && sender.hasPermission("strengthsmp.admin")) {
                String p = args[1].toLowerCase();
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (pl.getName().toLowerCase().startsWith(p)) completions.add(pl.getName());
                }
            } else if (args[0].equalsIgnoreCase("withdraw") && sender instanceof Player player) {
                int s = manager.getStrength(player);
                for (int i = 1; i <= s; i++) completions.add(String.valueOf(i));
            }
        }
        return completions;
    }

    private String prefix() { return "§6§l[Strength] §r"; }

    private String buildBar(int current, int max) {
        StringBuilder bar = new StringBuilder(" §8[");
        for (int i = 1; i <= max; i++) bar.append(i <= current ? "§6█" : "§7█");
        bar.append("§8]");
        return bar.toString();
    }
}
