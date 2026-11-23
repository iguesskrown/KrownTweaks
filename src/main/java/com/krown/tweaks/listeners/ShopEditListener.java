/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  de.marcely.bedwars.api.event.player.PlayerBuyInShopEvent
 *  de.marcely.bedwars.api.event.player.PlayerBuyInShopEvent$DefaultProblem
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 */
package com.krown.tweaks.listeners;

import com.krown.tweaks.KrownTweaksPlugin;
import de.marcely.bedwars.api.event.player.PlayerBuyInShopEvent;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ShopEditListener
implements Listener {
    private final KrownTweaksPlugin plugin;
    private final Set<UUID> editingPlayers = Collections.newSetFromMap(new ConcurrentHashMap());

    public ShopEditListener(KrownTweaksPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEditing(Player player) {
        return player != null && this.editingPlayers.contains(player.getUniqueId());
    }

    public void startEditing(Player player) {
        if (player != null) {
            this.editingPlayers.add(player.getUniqueId());
        }
    }

    public void stopEditing(Player player) {
        if (player != null) {
            this.editingPlayers.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerBuy(PlayerBuyInShopEvent event) {
        Player player = event.getPlayer();
        if (this.isEditing(player) && this.plugin.getConfig().getBoolean("shopedit.disable-purchases", true)) {
            event.setGivingProducts(false);
            event.setTakingPayments(false);
            event.addProblem(PlayerBuyInShopEvent.DefaultProblem.SILENT);
            player.sendMessage(this.plugin.getMessage("shopedit-buy-disabled"));
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        String msg;
        Player player;
        if (event.getPlayer() instanceof Player && this.editingPlayers.remove((player = (Player)event.getPlayer()).getUniqueId()) && (msg = this.plugin.getMessage("shopedit-saved")) != null && !msg.trim().isEmpty()) {
            boolean useActionBar = this.plugin.getConfig().getBoolean("shopedit.send-save-to-actionbar", true);
            if (useActionBar) {
                com.krown.tweaks.utils.ChatUtils.sendActionBar(player, msg);
            } else {
                player.sendMessage(msg);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.editingPlayers.remove(event.getPlayer().getUniqueId());
    }
}

