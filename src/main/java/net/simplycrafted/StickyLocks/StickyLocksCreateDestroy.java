package net.simplycrafted.StickyLocks;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;

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
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
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
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Block target = event.getBlock();
        if (target.getState() instanceof Chest) {
            // Double chests are hard to detect. Nevertheless, this will do it,
            // and we'll warn the player that the other portion might not be locked.
            Chest chest = (Chest) target.getState();
            InventoryHolder ih = chest.getInventory().getHolder();
            if (ih instanceof DoubleChest) {
                Protection protection = db.getProtection(target);
                if (protection.getType() != null) {
                    stickylocks.sendMessage(event.getPlayer(), "Check lock on remaining single chest", false);
                }
            }
        }
        db.unlockBlock(event.getBlock());
    }
}
