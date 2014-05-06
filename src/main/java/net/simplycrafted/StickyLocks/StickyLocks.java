package net.simplycrafted.StickyLocks;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
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

    public HashMap<Player,Location> SelectedBlock;

    @Override
    public void onEnable() {
        // Make sure the config's on disk and editable
        saveConfig();

        // We have the instance now. Keep it for convenience.
        stickylocks = this;

        // Commands are handled in this class.
        getCommand("stickylocks").setExecutor(new StickyLocksCommand());

        // Set up event handler for clicking blocks
        getServer().getPluginManager().registerEvents(new StickyLocksClick(),stickylocks);

        // Set up handler that detects new players joining
        getServer().getPluginManager().registerEvents(new StickyLocksPlayerjoin(),stickylocks);

        // Initialise database, and create tables if necessary
        db=new Database();
        db.createTables();

        // Which block players mighht have selected
        SelectedBlock = new HashMap<Player,Location>();
    }

    @Override
    public void onDisable() {
        PlayerInteractEvent.getHandlerList().unregister(stickylocks);
        db.shutdown();
        SelectedBlock.clear();
    }

    public static StickyLocks getInstance() {
        // Other classes might need the plugin instance. This saves going through the PluginManager.
        return stickylocks;
    }

    public void sendMessage(CommandSender player, String message, boolean locked) {
        if (locked)
            player.sendMessage(String.format("%s[%s]%s %s", ChatColor.GRAY,getConfig().getString("chatprefix"),ChatColor.DARK_RED,message));
        else
            player.sendMessage(String.format("%s[%s]%s %s", ChatColor.GRAY,getConfig().getString("chatprefix"),ChatColor.DARK_GREEN,message));
    }
}
