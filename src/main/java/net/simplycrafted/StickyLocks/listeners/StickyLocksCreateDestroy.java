package net.simplycrafted.StickyLocks.listeners;

import net.simplycrafted.StickyLocks.Database;
import net.simplycrafted.StickyLocks.DetectBuildLimiter;
import net.simplycrafted.StickyLocks.Protection;
import net.simplycrafted.StickyLocks.StickyLocks;
import net.simplycrafted.StickyLocks.util.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

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
    // or locks it for them, depending on their automatic lock bit.

    @EventHandler (priority = EventPriority.NORMAL)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        // Quit if we can't build here
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block target = event.getBlock();
        //clear any pre-existing locks that might exist due to chunk
        //regeneration, world edit or rollback operations
        db.unlockBlock(target);

        // Check whether the block is the second half of a chest. This information is
        // not available to this event (the chests merge later) so administratively
        // lock it, and schedule it for the next tick (synchronously).
        if (target.getType() == Material.CHEST || target.getType() == Material.TRAPPED_CHEST) {
            StickyLocks.getInstance().administrativeLock.add(target.getLocation());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Util.getOtherHalfOfChest(target) != null) {
                        Protection firstHalf = db.getProtection(Util.getOtherHalfOfChest(target));
                        if (firstHalf.isProtected() && firstHalf.getOwner() != null) {
                            // null owner indicates an administrative lock
                            db.duplicate(target, Util.getOtherHalfOfChest(target));
                        }
                    }
                    stickylocks.administrativeLock.remove(target.getLocation());
                }
            }.runTaskLater(stickylocks,1);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void checkBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block target = event.getBlock();
        if (player.hasPermission("stickylocks.lock")) {
            if (db.getAutomatic(player)) {
                if (!db.getProtection(target).isProtected()) {
                    db.lockBlock(target, player);
                }
            } else {
                // This message is sent if the chest isn't simply being extended, but that information isn't available until after this event.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Util.getOtherHalfOfChest(event.getBlockPlaced()) == null && db.isProtectable(event.getBlockPlaced().getType())) {
                            stickylocks.sendMuteableMessage(player, String.format("Right-click then left-click with %s to lock this object", stickylocks.getConfig().getString("tool")), true);
                        }
                    }
                }.runTaskLater(stickylocks,1);
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
                stickylocks.sendMuteableMessage(player,"Hopper placement blocked - access is denied to the inventory above",false, "Hopper placement cancelled, block above locked");
            }
        }
    }

    // This event handler responds to the destruction of a block. If the
    // block was protected, its data are removed from the database.
    //
    // Note that this handler does not cancel the destruction of blocks,
    // protected or not. Protecting blocks from being broken is a job
    // for another plugin.

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        db.unlockBlock(event.getBlock());
    }
}
