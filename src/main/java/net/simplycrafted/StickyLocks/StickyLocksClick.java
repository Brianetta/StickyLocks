package net.simplycrafted.StickyLocks;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

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
public class StickyLocksClick implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block target=event.getClickedBlock();
        //if target.getType() ...check it's a protectable block type
        if (event.getAction()== Action.RIGHT_CLICK_BLOCK) {
            // Initiate locking, or present actions for locked item
            String info = "";
            Location whereisit;
            info = info + target.getType().name();
            if (target.getState() instanceof Chest){
                // Double chests have an ambiguous location. Get a unique location by
                // asking the chest's Inventory for the location of its DoubleChest.
                Chest chest = (Chest) target.getState();
                InventoryHolder ih = chest.getInventory().getHolder();
                if (ih instanceof DoubleChest) {
                    DoubleChest dc = (DoubleChest) ih;
                    info = info + " " + dc.getLocation().getBlockX();
                    info = info + "," + dc.getLocation().getBlockY();
                    info = info + "," + dc.getLocation().getBlockZ();
                } else {
                    info = info + " " + target.getLocation().getBlockX();
                    info = info + "," + target.getLocation().getBlockY();
                    info = info + "," + target.getLocation().getBlockZ();
                }
            } else if (target.getType().name().equals("WOODEN_DOOR") && target.getRelative(BlockFace.DOWN).getType().name().equals("WOODEN_DOOR")) {
                // Doors have an ambiguous location, but we only need to check
                // the block below to disambiguate.
                info = info + " " + target.getLocation().getBlockX();
                info = info + "," + (target.getLocation().getBlockY()-1);
                info = info + "," + target.getLocation().getBlockZ();
            } else {
                info = info + " " + target.getLocation().getBlockX();
                info = info + "," + target.getLocation().getBlockY();
                info = info + "," + target.getLocation().getBlockZ();
            }
            event.getPlayer().sendMessage(info);
        }
    }
}