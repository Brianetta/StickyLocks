package net.simplycrafted.StickyLocks;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import javax.swing.text.html.parser.Entity;

/**
 * Copyright © Brian Ronald
 * 10/05/14
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
public class StickyLocksHopperMinecart implements Listener {
    Database db = new Database();
    private StickyLocks stickylocks = StickyLocks.getInstance();

    // This event handler checks for blocks moving around as with hoppers.
    // If it's found to be moving from a block (the inventory's holder is
    // a BlockState instance) to a minecart with hopper (the inventory's
    // holder is an instance of HopperMinecart) then the source is checked
    // for protection. If it's protected, the event is cancelled.
    //
    // The database is only checked if the inventory types are of concern.
    // This is to reduce load - hoppers events are expensive and widespread.

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getSource().getHolder() instanceof BlockState)) {
            return;
        }
        Block block = ((BlockState) event.getSource().getHolder()).getBlock();
        if (event.getDestination().getHolder() instanceof HopperMinecart) {
            Protection protection = db.getProtection(block);
            if (protection.isProtected()) {
                event.setCancelled(true);
            }
        }
    }
}
