package net.simplycrafted.StickyLocks;

import org.bukkit.Location;
import org.bukkit.Material;
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

    Material tool;
    Database db = new Database();

    // Get the tool item Material from the config
    public StickyLocksClick() {
        tool = Material.getMaterial(StickyLocks.getInstance().getConfig().getString("tool"));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        int locX,locY,locZ;

        Block target=event.getClickedBlock();
        //if target.getType() ...check it's a protectable block type
        if (event.getAction()== Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == tool) {
            // Initiate locking, or present actions for locked item
            String info = "";
            if (target.getState() instanceof Chest){
                // Double chests have an ambiguous location. Get a unique location by
                // asking the chest's Inventory for the location of its DoubleChest.
                Chest chest = (Chest) target.getState();
                InventoryHolder ih = chest.getInventory().getHolder();
                if (ih instanceof DoubleChest) {
                    DoubleChest dc = (DoubleChest) ih;
                    locX = dc.getLocation().getBlockX();
                    locY = dc.getLocation().getBlockY();
                    locZ = dc.getLocation().getBlockZ();
                } else {
                    locX = target.getLocation().getBlockX();
                    locY = target.getLocation().getBlockY();
                    locZ = target.getLocation().getBlockZ();
                }
            } else if (target.getType().name().equals("WOODEN_DOOR") && target.getRelative(BlockFace.DOWN).getType().name().equals("WOODEN_DOOR")) {
                // Doors have an ambiguous location, but we only need to check
                // the block below to disambiguate.
                locX = target.getLocation().getBlockX();
                locY = target.getLocation().getBlockY()-1;
                locZ = target.getLocation().getBlockZ();
            } else {
                locX = target.getLocation().getBlockX();
                locY = target.getLocation().getBlockY();
                locZ = target.getLocation().getBlockZ();
            }
            if (db.isProtectable(target)) {
                event.getPlayer().sendMessage(String.format("%s (%d,%d,%d)", target.getType().name(), locX, locY, locZ));
                event.setCancelled(true);
            }
        }
    }
}