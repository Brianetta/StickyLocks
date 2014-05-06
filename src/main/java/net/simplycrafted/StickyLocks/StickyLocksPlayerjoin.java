package net.simplycrafted.StickyLocks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
    static StickyLocks stickylocks;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        db.addPlayer(event.getPlayer());
    }
}
