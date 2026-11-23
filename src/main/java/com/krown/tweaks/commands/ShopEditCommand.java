/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  de.marcely.bedwars.api.BedwarsAPI
 *  de.marcely.bedwars.api.arena.Arena
 *  de.marcely.bedwars.api.game.shop.ShopOpenCause
 *  de.marcely.bedwars.api.game.shop.layout.ShopLayout
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package com.krown.tweaks.commands;

import com.krown.tweaks.KrownTweaksPlugin;
import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.game.shop.ShopOpenCause;
import de.marcely.bedwars.api.game.shop.layout.ShopLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ShopEditCommand
implements CommandExecutor,
TabCompleter {
    private final KrownTweaksPlugin plugin;

    public ShopEditCommand(KrownTweaksPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean inGame;
        Player target;
        if (!this.plugin.getConfig().getBoolean("shopedit.enabled", true)) {
            sender.sendMessage(this.plugin.getMessage("command-disabled"));
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(this.plugin.getMessage("player-only"));
                return true;
            }
            if (!sender.hasPermission("krowntweaks.shopedit")) {
                sender.sendMessage(this.plugin.getMessage("no-permission"));
                return true;
            }
            target = (Player)sender;
        } else {
            if (!sender.hasPermission("krowntweaks.shopedit.admin")) {
                sender.sendMessage(this.plugin.getMessage("no-permission"));
                return true;
            }
            target = Bukkit.getPlayer((String)args[0]);
            if (target == null || !target.isOnline()) {
                HashMap<String, String> ph = new HashMap<String, String>();
                ph.put("target", args[0]);
                sender.sendMessage(this.plugin.getMessage("player-not-found", ph));
                return true;
            }
        }
        Arena arena = BedwarsAPI.getGameAPI().getArenaByPlayer(target);
        boolean bl = inGame = arena != null;
        if (inGame && !this.plugin.getConfig().getBoolean("shopedit.allow-in-game", true)) {
            sender.sendMessage(this.plugin.getMessage("shopedit-cannot-in-game"));
            return true;
        }
        if (!inGame && !this.plugin.getConfig().getBoolean("shopedit.allow-in-lobby", true)) {
            sender.sendMessage(this.plugin.getMessage("shopedit-cannot-in-lobby"));
            return true;
        }
        String layoutName = this.plugin.getConfig().getString("shopedit.layout", "HypixelV2");
        if (!sender.equals((Object)target)) {
            HashMap<String, String> placeholders = new HashMap<String, String>();
            placeholders.put("layout", layoutName);
            placeholders.put("target", target.getName());
            String adminMsg = this.plugin.getMessage("shopedit-opened-by-admin", placeholders);
            if (adminMsg != null && !adminMsg.trim().isEmpty()) {
                target.sendMessage(adminMsg);
            }
        }
        this.plugin.getShopEditListener().startEditing(target);
        this.openShopLayout(target, layoutName);
        return true;
    }

    private void openShopLayout(Player target, String layoutName) {
        try {
            ShopLayout layout = BedwarsAPI.getGameAPI().getShopLayout(layoutName.toLowerCase());
            if (layout == null) {
                layout = BedwarsAPI.getGameAPI().getShopLayout(layoutName);
            }
            if (layout != null) {
                BedwarsAPI.getGameAPI().openShop(target, layout, ShopOpenCause.COMMAND);
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)("bw tools openshop " + target.getName() + " " + layoutName));
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("krowntweaks.shopedit.admin")) {
            ArrayList<String> list = new ArrayList<String>();
            String current = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getName().toLowerCase().startsWith(current)) continue;
                list.add(player.getName());
            }
            return list;
        }
        return Collections.emptyList();
    }
}

