/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 */
package com.krown.tweaks.commands;

import com.krown.tweaks.KrownTweaksPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class KrownTweaksCommand
implements CommandExecutor,
TabCompleter {
    private final KrownTweaksPlugin plugin;

    public KrownTweaksCommand(KrownTweaksPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("krowntweaks.admin")) {
            sender.sendMessage(this.plugin.getMessage("no-permission"));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadConfig();
            sender.sendMessage(this.plugin.getMessage("reload-success"));
            return true;
        }
        sender.sendMessage(this.plugin.getPrefix() + "\u00a7eKrownTweaks v" + this.plugin.getDescription().getVersion() + " (MC 1.21.1)");
        sender.sendMessage("\u00a77- \u00a7b/shopedit [player] \u00a78- \u00a77Opens Quick Buy editor");
        sender.sendMessage("\u00a77- \u00a7b/spectate <player> \u00a78- \u00a77Spectate a player in BedWars");
        sender.sendMessage("\u00a77- \u00a7b/" + label + " reload \u00a78- \u00a77Reloads configuration");
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("krowntweaks.admin")) {
            ArrayList<String> list = new ArrayList<String>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                list.add("reload");
            }
            return list;
        }
        return Collections.emptyList();
    }
}

