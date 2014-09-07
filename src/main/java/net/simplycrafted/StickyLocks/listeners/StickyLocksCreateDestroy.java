package net.simplycrafted.StickyLocks.listeners;

import net.simplycrafted.StickyLocks.Database;
import net.simplycrafted.StickyLocks.DetectBuildLimiter;
import net.simplycrafted.StickyLocks.Protection;
import net.simplycrafted.StickyLocks.StickyLocks;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    private DetectBuildLimiter detectBuildLimiter = new DetectBuildLimiter();

    // this event handler responds to a block place event, and either
    // informs the player that they can lock a chest with the tool,
    // or locks it for them, depending on the autolock config setting.

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        // Quit if we can't build here
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block target = event.getBlock();
        if (player.hasPermission("stickylocks.lock")) {
            if (stickylocks.getConfig().getBoolean("autolock")) {
                Protection protection = db.getProtection(target);
                if (protection.getType() != null) {
                    db.lockBlock(target, player);
                }
            } else {
                if (db.isProtectable(event.getBlockPlaced().getType())) {
                    stickylocks.sendMessage(player, String.format("Right-click then left-click with %s to lock this object", stickylocks.getConfig().getString("tool")), true);
                }
            }
        }
    }

    // this event handler responds to a block place event, checks if
    // it is a hopper, and blocks it if the block above is protected,
    // not accessible by the player, and has an inventory.

    @EventHandler (priority = EventPriority.NORMAL)
    public void onHopperPlaceEvent(BlockPlaceEvent event) {
        // Quit if we can't build here
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block target = event.getBlock();
        if (target.getType()== Material.HOPPER) {
            // Got a hopper - switch target to the block above it
            target = target.getRelative(BlockFace.UP);
            if (target.getState() instanceof InventoryHolder & db.getProtection(target).isProtected() & db.accessDenied(player,target)) {
                // The block above the hopper has an inventory, and it's not ours - cancel!
                event.setCancelled(true);
                stickylocks.sendMessage(player,"Hopper placement blocked - access is denied to the inventory above",false);
            }
        }
    }

    // This event handler responds to the destruction of a block. If the
    // block was protected, its data are removed from the database. If
    // it was a double chest (which implies that a single chest remains)
    // there's a 50/50 chance that the remaining single chest is no longer
    // locked. This handler also warns the player.
    //
    // Note that this handler does not cancel the destruction of blocks,
    // protected or not. Protecting blocks from being broken is a job
    // for another plugin.

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block target = event.getBlock();
        if (target.getState() instanceof Chest) {
            // Double chests are hard to detect. Nevertheless, this will do it,
            // and we'll warn the player that the other portion might not be locked.
            Chest chest = (Chest) target.getState();
            InventoryHolder ih = chest.getInventory().getHolder();
            if (ih instanceof DoubleChest) {
                Protection protection = db.getProtection(target);
                if (protection.isProtected()) {
                    stickylocks.sendMessage(event.getPlayer(), "Check lock on remaining single chest", false);
                }
            }
        }
        db.unlockBlock(event.getBlock());
    }
}
