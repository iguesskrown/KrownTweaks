/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  de.marcely.bedwars.api.BedwarsAPI
 *  de.marcely.bedwars.api.arena.Arena
 *  de.marcely.bedwars.api.arena.ArenaStatus
 *  de.marcely.bedwars.api.game.spectator.KickSpectatorReason
 *  de.marcely.bedwars.api.game.spectator.SpectateReason
 *  de.marcely.bedwars.api.game.spectator.Spectator
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.krown.tweaks.commands;

import com.krown.tweaks.KrownTweaksPlugin;
import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.game.spectator.KickSpectatorReason;
import de.marcely.bedwars.api.game.spectator.SpectateReason;
import de.marcely.bedwars.api.game.spectator.Spectator;
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
import org.bukkit.plugin.Plugin;

public class SpectateCommand
implements CommandExecutor,
TabCompleter {
    private final KrownTweaksPlugin plugin;

    public SpectateCommand(KrownTweaksPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean allowWhileAlive;
        if (!this.plugin.getConfig().getBoolean("spectate.enabled", true)) {
            sender.sendMessage(this.plugin.getMessage("command-disabled"));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.getMessage("player-only"));
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("krowntweaks.spectate")) {
            player.sendMessage(this.plugin.getMessage("no-permission"));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(this.plugin.getMessage("spectate-usage"));
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null || !target.isOnline()) {
            HashMap<String, String> ph = new HashMap<String, String>();
            ph.put("target", args[0]);
            player.sendMessage(this.plugin.getMessage("player-not-found", ph));
            return true;
        }
        if (player.equals((Object)target)) {
            player.sendMessage(this.plugin.getMessage("cannot-spectate-self"));
            return true;
        }
        Arena targetArena = BedwarsAPI.getGameAPI().getArenaByPlayer(target);
        if (targetArena == null) {
            targetArena = BedwarsAPI.getGameAPI().getArenaBySpectator(target);
        }
        if (targetArena == null) {
            HashMap<String, String> ph = new HashMap<String, String>();
            ph.put("target", target.getName());
            player.sendMessage(this.plugin.getMessage("target-not-in-bedwars", ph));
            return true;
        }
        if (this.plugin.getConfig().getBoolean("spectate.require-running-game", true) && targetArena.getStatus() != ArenaStatus.RUNNING) {
            HashMap<String, String> ph = new HashMap<String, String>();
            ph.put("arena", targetArena.getName());
            ph.put("target", target.getName());
            player.sendMessage(this.plugin.getMessage("arena-not-running", ph));
            return true;
        }
        Arena senderPlayingArena = BedwarsAPI.getGameAPI().getArenaByPlayer(player);
        if (senderPlayingArena != null && !(allowWhileAlive = this.plugin.getConfig().getBoolean("spectate.allow-while-alive", false)) && !player.hasPermission("krowntweaks.spectate.bypass")) {
            player.sendMessage(this.plugin.getMessage("cannot-spectate-while-alive"));
            return true;
        }
        Arena senderSpectatingArena = BedwarsAPI.getGameAPI().getArenaBySpectator(player);
        if (senderSpectatingArena != null && senderSpectatingArena.equals((Object)targetArena)) {
            player.teleport(target.getLocation());
            HashMap<String, String> ph = new HashMap<String, String>();
            ph.put("target", target.getName());
            ph.put("arena", targetArena.getName());
            player.sendMessage(this.plugin.getMessage("teleported-to-target", ph));
            return true;
        }
        if (senderSpectatingArena != null && !senderSpectatingArena.equals((Object)targetArena)) {
            if (!this.plugin.getConfig().getBoolean("spectate.allow-cross-arena", true)) {
                player.sendMessage(this.plugin.getMessage("cannot-spectate-cross-arena"));
                return true;
            }
            try {
                Spectator spec = senderSpectatingArena.getSpectateData(player);
                if (spec != null) {
                    spec.kick(KickSpectatorReason.PLUGIN);
                }
            }
            catch (Throwable spec) {
                // empty catch block
            }
        }
        targetArena.addSpectator(player, SpectateReason.PLUGIN);
        long delayTicks = Math.max(1L, this.plugin.getConfig().getLong("spectate.teleport-delay-ticks", 2L));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (player.isOnline() && target.isOnline()) {
                player.teleport(target.getLocation());
            }
        }, delayTicks);
        HashMap<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("target", target.getName());
        placeholders.put("arena", targetArena.getName());
        player.sendMessage(this.plugin.getMessage("spectating-success", placeholders));
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> matches = new ArrayList<String>();
            String prefix = args[0].toLowerCase();
            for (Player target : Bukkit.getOnlinePlayers()) {
                Arena arena;
                if (sender instanceof Player && target.equals((Object)sender) || !target.getName().toLowerCase().startsWith(prefix) || (arena = BedwarsAPI.getGameAPI().getArenaByPlayer(target)) == null) continue;
                matches.add(target.getName());
            }
            if (matches.isEmpty()) {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (sender instanceof Player && target.equals((Object)sender) || !target.getName().toLowerCase().startsWith(prefix)) continue;
                    matches.add(target.getName());
                }
            }
            return matches;
        }
        return Collections.emptyList();
    }
}

