package net.simplycrafted.StickyLocks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Copyright © Brian Ronald
 * 09/05/14
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
public class StickyLocksCreateDestroy implements Listener {
    Database db = new Database();
    private StickyLocks stickylocks = StickyLocks.getInstance();

    @EventHandler
    public void onPlayerJoin(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block target = event.getBlock();
        if (player.hasPermission("stickylocks.lock")) {
            if (stickylocks.getConfig().getBoolean("autolock")) {
                Protection protection = db.getProtection(target);
                if (protection.getType() != null) {
                    db.lockBlock(target, player);
                }
            } else {
                stickylocks.sendMessage(player, String.format("Right-click then left-click with %s to lock this object",stickylocks.getConfig().getString("tool")),true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(BlockBreakEvent event) {
        db.unlockBlock(event.getBlock());
    }
}
