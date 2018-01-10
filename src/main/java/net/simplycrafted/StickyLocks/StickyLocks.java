package net.simplycrafted.StickyLocks;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.simplycrafted.StickyLocks.commands.StickyLocksCommand;
import net.simplycrafted.StickyLocks.listeners.StickyLocksClick;
import net.simplycrafted.StickyLocks.listeners.StickyLocksCreateDestroy;
import net.simplycrafted.StickyLocks.listeners.StickyLocksHopperMinecart;
import net.simplycrafted.StickyLocks.listeners.StickyLocksPlayerjoin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/**
 * Copyright © Brian Ronald
 * 04/05/14
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

public class StickyLocks extends JavaPlugin {

    // There can be only one instance of this class. Store it here.
    private static StickyLocks stickylocks;

    private static Database db;

    public HashMap<Player,Location> selectedBlock;
    public HashMap<Player,Boolean> playerNotification;

    @Override
    public void onEnable() {
        // Make sure the config's on disk and editable
        saveDefaultConfig();

        // We have the instance now. Keep it for convenience.
        stickylocks = this;

        // Commands are handled in this class.
        getCommand("stickylocks").setExecutor(new StickyLocksCommand());

        // Set up event handler for clicking blocks
        getServer().getPluginManager().registerEvents(new StickyLocksClick(),stickylocks);

        // Set up handler that detects new players joining
        getServer().getPluginManager().registerEvents(new StickyLocksPlayerjoin(),stickylocks);

        // Set up handler that detects blocks being created or destroyed
        getServer().getPluginManager().registerEvents(new StickyLocksCreateDestroy(),stickylocks);

        // Set up handler that prevents robbing using hopper minecarts
        getServer().getPluginManager().registerEvents(new StickyLocksHopperMinecart(),stickylocks);

        // Initialise database, and create tables if necessary
        db=new Database();
        db.createTables();

        // Which block players might have selected
        selectedBlock = new HashMap<>();

        // Per-player notification settings
        playerNotification = new HashMap<>();
    }

    @Override
    public void onDisable() {
        // Tidy up our handlers
        PlayerInteractEvent.getHandlerList().unregister(stickylocks);
        InventoryMoveItemEvent.getHandlerList().unregister(stickylocks);
        PlayerJoinEvent.getHandlerList().unregister(stickylocks);
        BlockPlaceEvent.getHandlerList().unregister(stickylocks);
        BlockBreakEvent.getHandlerList().unregister(stickylocks);
        // Clear away the database class
        db.shutdown();
        // Clear the block selections
        selectedBlock.clear();
    }

    public static StickyLocks getInstance() {
        // Other classes might need the plugin instance. This saves going through the PluginManager.
        return stickylocks;
    }

    public void sendMessage(CommandSender player, String message, boolean unlocked) {
        player.sendMessage(String.format("%s[%s]%s %s", ChatColor.GRAY, getConfig().getString("chatprefix"), unlocked ? ChatColor.DARK_GREEN : ChatColor.DARK_RED, message));
    }

    public void sendMuteableMessage(CommandSender player, String message, boolean unlocked) {
        sendMuteableMessage(player, message, unlocked, null);
    }

    public void sendMuteableMessage(CommandSender player, String message, boolean unlocked, String altMessage) {
        // "unlocked" is a colour flag. If true, message is green. If not, message is red.
        if(playerNotification.get(player)) {
            // Chat message
            player.sendMessage(String.format("%s[%s]%s %s", ChatColor.GRAY, getConfig().getString("chatprefix"), unlocked ? ChatColor.DARK_GREEN : ChatColor.DARK_RED, message));
        } else if (altMessage != null & player instanceof Player) {
            // Brief action bar pop-up in red or green
            ((Player) player).spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(altMessage).color(unlocked ? net.md_5.bungee.api.ChatColor.DARK_GREEN : net.md_5.bungee.api.ChatColor.DARK_RED).create());
        }
    }
}
