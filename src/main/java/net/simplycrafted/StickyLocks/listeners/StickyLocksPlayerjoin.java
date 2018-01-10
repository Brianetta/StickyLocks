package net.simplycrafted.StickyLocks.listeners;

import net.simplycrafted.StickyLocks.Database;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static net.simplycrafted.StickyLocks.listeners.StickyLocksClick.stickylocks;

/**
 * Copyright © Brian Ronald
 * 06/05/14
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
public class StickyLocksPlayerjoin implements Listener {
    Database db = new Database();

    // This event handler detects when a player joins the server, and
    // inserts or updates their entry in the database, allowing the
    // plugin to deal with the player's name when they're offline.

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        db.addPlayer(event.getPlayer());
        stickylocks.playerNotification.put(event.getPlayer(),db.getNotification(event.getPlayer()));
    }
}
